package ru.team.compiler.compilator;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.compilator.constant.ClassConstant;
import ru.team.compiler.compilator.constant.ConstantPool;
import ru.team.compiler.compilator.constant.FieldRefConstant;
import ru.team.compiler.compilator.constant.MethodRefConstant;
import ru.team.compiler.compilator.constant.NameAndTypeConstant;
import ru.team.compiler.compilator.constant.Utf8Constant;
import ru.team.compiler.tree.node.clas.ConstructorNode;
import ru.team.compiler.tree.node.clas.FieldNode;
import ru.team.compiler.tree.node.clas.MethodNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

public final class CompilationUtils {

    public static final String OLANG_PACKAGE = "olang";

    private CompilationUtils() {

    }

    @NotNull
    public static String descriptor(@NotNull FieldNode fieldNode) {
        return descriptor(fieldNode.type());
    }

    @NotNull
    public static String descriptor(@NotNull MethodNode methodNode) {
        ParametersNode parameters = methodNode.parameters();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (ParametersNode.Par par : parameters.pars()) {
            stringBuilder.append(descriptor(par.type()));
        }
        stringBuilder.append(")");

        ReferenceNode returnType = methodNode.returnType();
        stringBuilder.append(returnType != null ? descriptor(returnType) : "V");

        return stringBuilder.toString();
    }

    @NotNull
    public static String descriptor(@NotNull ConstructorNode constructorNode) {
        return descriptor(
                new MethodNode(
                        false, new IdentifierNode("<init>"),
                        constructorNode.parameters(), null, constructorNode.body()
                )
        );
    }

    @NotNull
    public static String descriptor(@NotNull IdentifierNode identifierNode) {
        return descriptor(identifierNode.asReference());
    }

    @NotNull
    public static String descriptor(@NotNull ReferenceNode referenceNode) {
        return "L" + OLANG_PACKAGE + "/" + referenceNode.value() + ";";
    }

    @NotNull
    public static ClassConstant oClass(@NotNull ConstantPool constantPool, @NotNull String name) {
        return constantPool.getClass(constantPool.getUtf(OLANG_PACKAGE + "/" + name));
    }

    @NotNull
    public static MethodRefConstant oMethod(@NotNull ConstantPool constantPool, @NotNull String declaredClass,
                                            @NotNull MethodNode methodNode) {
        return oMethod(constantPool, declaredClass, methodNode.name().value(), descriptor(methodNode));
    }

    @NotNull
    public static MethodRefConstant oMethod(@NotNull ConstantPool constantPool, @NotNull String declaredClass,
                                            @NotNull ConstructorNode constructorNode) {
        return oMethod(constantPool, declaredClass, "<init>", descriptor(constructorNode));
    }

    @NotNull
    public static MethodRefConstant oMethod(@NotNull ConstantPool constantPool, @NotNull String declaredClass,
                                            @NotNull String name, @NotNull String descriptor) {
        ClassConstant classConstant = oClass(constantPool, declaredClass);

        Utf8Constant nameConstant = constantPool.getUtf(name);
        Utf8Constant descriptorConstant = constantPool.getUtf(descriptor);

        NameAndTypeConstant nameAndTypeConstant = constantPool.getNameAndType(nameConstant, descriptorConstant);

        return constantPool.getMethodRef(classConstant, nameAndTypeConstant);
    }

    @NotNull
    public static FieldRefConstant oField(@NotNull ConstantPool constantPool, @NotNull String declaredClass,
                                          @NotNull FieldNode fieldNode) {
        ClassConstant classConstant = oClass(constantPool, declaredClass);

        Utf8Constant nameConstant = constantPool.getUtf(fieldNode.name().value());
        Utf8Constant descriptorConstant = constantPool.getUtf(descriptor(fieldNode));

        NameAndTypeConstant nameAndTypeConstant = constantPool.getNameAndType(nameConstant, descriptorConstant);

        return constantPool.getFieldRef(classConstant, nameAndTypeConstant);
    }
}
