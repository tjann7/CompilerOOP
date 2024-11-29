package ru.team.compiler.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.analyzer.AnalyzableClass;
import ru.team.compiler.analyzer.AnalyzableMethod;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.compiler.attribute.CodeAttribute;
import ru.team.compiler.compiler.constant.ClassConstant;
import ru.team.compiler.compiler.constant.ConstantPool;
import ru.team.compiler.compiler.constant.Utf8Constant;
import ru.team.compiler.tree.node.clas.ClassMemberNode;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.ConstructorNode;
import ru.team.compiler.tree.node.clas.FieldNode;
import ru.team.compiler.tree.node.clas.MethodNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.primary.ThisNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.MethodCallNode;
import ru.team.compiler.tree.node.statement.ReturnNode;
import ru.team.compiler.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record ClassFile(@NotNull ConstantPool constantPool, boolean isAbstract,
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
            } else if (classMemberNode instanceof ConstructorNode constructorNode) {
                CompilationMethod method = CompilationMethod.fromNode(constantPool, classNode, constructorNode);
                methods.add(method);
            }
        }

        AnalyzableClass currentClass = analyzeContext.classes().get(classNode.name().asReference());

        Map<AnalyzableMethod.Key, AnalyzableMethod> thisMethods = currentClass.methods();

        AnalyzableClass currentParentClass = currentClass;
        while (true) {
            currentParentClass = currentParentClass.findParentClass(analyzeContext, "Class");
            if (currentParentClass == null) {
                break;
            }

            Set<Pair<AnalyzableMethod.Key, ReferenceNode>> bridges = new HashSet<>();

            for (AnalyzableMethod superMethod : currentParentClass.methods().values()) {
                AnalyzableMethod.Key key = superMethod.key();
                AnalyzableMethod thisMethod = thisMethods.get(key);
                if (thisMethod == null) {
                    continue;
                }

                Pair<AnalyzableMethod.Key, ReferenceNode> pair = Pair.of(key, superMethod.returnType());
                if (!bridges.add(pair)) {
                    continue;
                }

                IdentifierNode methodName = superMethod.name();
                Utf8Constant name = constantPool.getUtf(methodName.value());
                Utf8Constant descriptor = constantPool.getUtf(CompilationUtils.descriptor(superMethod.methodNode()));

                ReferenceNode returnType = superMethod.returnType();
                if (!Objects.equals(thisMethod.returnType(), returnType)) {
                    ParametersNode parameters = superMethod.parameters();

                    ExpressionNode expression = new ExpressionNode(
                            new ThisNode(),
                            List.of(new ExpressionNode.IdArg(
                                    methodName,
                                    new ArgumentsNode(
                                            parameters.pars().stream()
                                                    .map(par -> new ExpressionNode(par.type(), List.of()))
                                                    .collect(Collectors.toList())))));

                    MethodNode methodNode = new MethodNode(
                            false, false, methodName, parameters, returnType,
                            new BodyNode(List.of(
                                    returnType != null
                                            ? new ReturnNode(expression)
                                            : new MethodCallNode(expression))));

                    CodeAttribute codeAttribute = new CodeAttribute(constantPool, classNode,
                            methodNode);

                    // Bridge and synthetic
                    int modifiers = 0x0040 | 0x1000;

                    methods.add(new CompilationMethod(name, descriptor, codeAttribute, modifiers));
                }
            }
        }

        return new ClassFile(
                constantPool,
                classNode.isAbstract(),
                classNode.name().value(),
                parentName != null ? parentName.value() : null,
                fields,
                methods
        );
    }

    public int accessFlags() {
        return Modifier.PUBLIC | (isAbstract ? Modifier.ABSTRACT : 0);
    }

    public void compile(@NotNull CompilationContext context, @NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(0xCAFEBABE);

        dataOutput.writeShort(0); // Java 6
        dataOutput.writeShort(50);

        ClassConstant thisClass = CompilationUtils.oClass(constantPool, className);

        ClassConstant superClass;
        if (superClassName != null) {
            if (superClassName.equals("")) {
                superClass = constantPool.getClass(constantPool.getUtf("java/lang/Object"));
            } else {
                superClass = CompilationUtils.oClass(constantPool, superClassName);
            }
        } else {
            superClass = constantPool.getClass(constantPool.getUtf("olang/Any"));
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
