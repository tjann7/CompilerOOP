package ru.team.compiler.compilator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.compilator.constant.ClassConstant;
import ru.team.compiler.compilator.constant.ConstantPool;
import ru.team.compiler.tree.node.clas.ClassMemberNode;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.FieldNode;
import ru.team.compiler.tree.node.clas.MethodNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public record ClassFile(@NotNull ConstantPool constantPool,
                        @NotNull String className, @Nullable String superClassName,
                        @NotNull List<CompilationField> fields,
                        @NotNull List<CompilationMethod> methods) {

    @NotNull
    public static ClassFile fromNode(@NotNull AnalyzeContext analyzeContext, @NotNull ClassNode classNode) {
        ReferenceNode parentName = classNode.parentName();

        ConstantPool constantPool = new ConstantPool();

        List<CompilationField> fields = new ArrayList<>();
        List<CompilationMethod> methods = new ArrayList<>();

        for (ClassMemberNode classMemberNode : classNode.classMembers()) {
            if (classMemberNode instanceof FieldNode fieldNode) {
                CompilationField field = CompilationField.fromNode(constantPool, classNode, fieldNode);
                fields.add(field);
            } else if (classMemberNode instanceof MethodNode methodNode) {
                CompilationMethod method = CompilationMethod.fromNode(constantPool, classNode, methodNode);
                methods.add(method);
            }
        }

        return new ClassFile(
                constantPool,
                classNode.name().value(),
                parentName != null ? parentName.value() : null,
                fields,
                methods
        );
    }

    public int accessFlags() {
        return Modifier.PUBLIC;
    }

    public void compile(@NotNull CompilationContext context, @NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(0xCAFEBABE);

        dataOutput.writeShort(52); // Java 8
        dataOutput.writeShort(0);

        ClassConstant thisClass = CompilationUtils.oClass(constantPool, className);

        ClassConstant superClass;
        if (superClassName != null) {
            if (superClassName.equals("")) {
                superClass = constantPool.getClass(constantPool.getUtf("olang/Any"));
            } else {
                superClass = CompilationUtils.oClass(constantPool, superClassName);
            }
        } else {
            superClass = constantPool.getClass(constantPool.getUtf("java/lang/Object"));
        }

        // Firstly, compile methods and fields, because they change ConstantPool
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        DataOutputStream byteDataOutput = new DataOutputStream(byteArrayOutputStream);

        byteDataOutput.writeShort(accessFlags());

        byteDataOutput.writeShort(thisClass.index());
        byteDataOutput.writeShort(superClass.index());

        // Interfaces
        byteDataOutput.writeShort(0);

        // Fields
        byteDataOutput.writeShort(fields.size());
        for (CompilationField field : fields) {
            field.compile(constantPool, byteDataOutput);
        }

        // Methods
        byteDataOutput.writeShort(methods.size());
        for (CompilationMethod method : methods) {
            method.compile(context, constantPool, byteDataOutput);
        }

        // Attributes
        byteDataOutput.writeShort(0);

        // Secondly, compile ConstantPool and add everything else
        constantPool.compile(dataOutput);

        dataOutput.write(byteArrayOutputStream.toByteArray());
    }
}
