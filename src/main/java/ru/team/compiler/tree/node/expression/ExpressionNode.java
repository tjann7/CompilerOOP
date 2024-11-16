package ru.team.compiler.tree.node.expression;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.team.compiler.analyzer.AnalyzableClass;
import ru.team.compiler.analyzer.AnalyzableConstructor;
import ru.team.compiler.analyzer.AnalyzableField;
import ru.team.compiler.analyzer.AnalyzableMethod;
import ru.team.compiler.analyzer.AnalyzableVariable;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.compiler.CompilationContext;
import ru.team.compiler.compiler.CompilationUtils;
import ru.team.compiler.compiler.attribute.CodeAttribute;
import ru.team.compiler.compiler.constant.ClassConstant;
import ru.team.compiler.compiler.constant.ConstantPool;
import ru.team.compiler.compiler.constant.FieldRefConstant;
import ru.team.compiler.compiler.constant.MethodRefConstant;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;
import ru.team.compiler.tree.node.primary.IntegerLiteralNode;
import ru.team.compiler.tree.node.primary.PrimaryNode;
import ru.team.compiler.tree.node.primary.RealLiteralNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.primary.ThisNode;
import ru.team.compiler.tree.node.statement.MethodCallNode;
import ru.team.compiler.util.Opcodes;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class ExpressionNode extends TreeNode {

    public static final TreeNodeParser<ExpressionNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ExpressionNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            PrimaryNode primary = PrimaryNode.PARSER.parse(iterator);

            List<IdArg> idArgs = new ArrayList<>();

            if (primary instanceof ReferenceNode && iterator.lookup(TokenType.OPENING_PARENTHESIS)) {
                ArgumentsNode argumentsNode = ArgumentsNode.PARSER.parse(iterator);

                idArgs.add(new IdArg(new IdentifierNode("<init>"), argumentsNode));
            }

            while (iterator.hasNext()) {
                if (iterator.consume(TokenType.DOT)) {
                    IdentifierNode identifierNode = IdentifierNode.PARSER.parse(iterator);

                    ArgumentsNode argumentsNode;

                    if (iterator.lookup(TokenType.OPENING_PARENTHESIS)) {
                        argumentsNode = ArgumentsNode.PARSER.parse(iterator);
                    } else {
                        argumentsNode = null;
                    }

                    idArgs.add(new IdArg(identifierNode, argumentsNode));
                } else {
                    break;
                }
            }

            return new ExpressionNode(primary, idArgs);
        }
    };

    private final PrimaryNode primary;
    private final List<IdArg> idArgs;

    public ExpressionNode(@NotNull PrimaryNode primary, @NotNull List<IdArg> idArgs) {
        this.primary = primary;
        this.idArgs = List.copyOf(idArgs);
    }

    @NotNull
    public PrimaryNode primary() {
        return primary;
    }

    @NotNull
    @Unmodifiable
    public List<IdArg> idArgs() {
        return idArgs;
    }

    @NotNull
    public ExpressionNode withIdArgs(@NotNull List<IdArg> idArgs) {
        List<IdArg> newIdArgs = new ArrayList<>(this.idArgs.size() + idArgs.size());
        newIdArgs.addAll(this.idArgs);
        newIdArgs.addAll(idArgs);

        return new ExpressionNode(primary, newIdArgs);
    }

    @Nullable
    public MethodCallNode asMethodCall() {
        for (int j = idArgs.size() - 1; j >= 0; j--) {
            IdArg idArg = idArgs.get(j);
            if (idArg.arguments != null) {
                return new MethodCallNode(new ExpressionNode(primary, idArgs.subList(0, j + 1)));
            }
        }

        return null;
    }

    @Override
    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        type(context, false);
        return context;
    }

    @NotNull
    public ReferenceNode type(@NotNull AnalyzeContext context, boolean allowVoid) {
        ReferenceNode currentType;

        int shift = 0;

        if (primary instanceof ReferenceNode referenceNode) {
            AnalyzableClass analyzableClass = context.classes().get(referenceNode);
            if (analyzableClass != null) {
                if (idArgs.isEmpty()) {
                    throw new AnalyzerException("Expression at '%s' is invalid: reference to type"
                            .formatted(context.currentPath()));
                }

                IdArg idArg = idArgs.get(0);
                if (!idArg.name.value().equals("<init>")) {
                    throw new AnalyzerException("Expression at '%s' is invalid: reference to static"
                            .formatted(context.currentPath()));
                }

                if (idArg.arguments == null) {
                    throw new AnalyzerException("Expression at '%s' is invalid: no arguments for constructor"
                            .formatted(context.currentPath()));
                }

                AnalyzableConstructor constructor = context.findMatchingExecutable(
                        analyzableClass,
                        idArg.arguments,
                        currentClass -> new ArrayList<>(currentClass.constructors().values()),
                        AnalyzableConstructor::parameters);

                if (constructor == null) {
                    List<ReferenceNode> argumentsTypes = context.argumentTypes(idArg.arguments);

                    throw new AnalyzerException("Expression at '%s' is invalid: reference to unknown constructor '(%s)' in type '%s'"
                            .formatted(
                                    context.currentPath(),
                                    argumentsTypes.stream()
                                            .map(ReferenceNode::value)
                                            .collect(Collectors.joining(",")),
                                    referenceNode.value()));
                }

                shift = 1;

                currentType = referenceNode;
            } else {
                if (!idArgs.isEmpty() && idArgs.get(0).name.value().equals("<init>")) {
                    throw new AnalyzerException("Expression at '%s' is invalid: reference to unknown type '%s'"
                            .formatted(context.currentPath(), referenceNode.value()));
                }

                AnalyzableVariable variable = context.variables().get(referenceNode);
                if (variable == null) {
                    throw new AnalyzerException("Expression at '%s' is invalid: reference to unknown variable '%s'"
                            .formatted(context.currentPath(), referenceNode.value()));
                }

                if (!context.initializedVariables().contains(variable.name().asReference())) {
                    throw new AnalyzerException("Expression at '%s' is invalid: reference to uninitialized variable '%s'"
                            .formatted(context.currentPath(), referenceNode.value()));
                }

                currentType = variable.type();
            }
        } else if (primary instanceof BooleanLiteralNode) {
            currentType = new ReferenceNode("Boolean");
        } else if (primary instanceof IntegerLiteralNode) {
            currentType = new ReferenceNode("Integer");
        } else if (primary instanceof RealLiteralNode) {
            currentType = new ReferenceNode("Real");
        } else if (primary instanceof ThisNode) {
            AnalyzableClass currentClass = context.currentClass("Expression");

            if (context.currentMethod() == null && !idArgs.isEmpty()) {
                IdArg idArg = idArgs.get(0);

                // TODO: maybe also forbid method calls if all fields are not initialized
                if (idArg.arguments == null && !context.initializedFields().contains(idArg.name.asReference())) {
                    throw new AnalyzerException("Expression at '%s' is invalid: reference to uninitialized field '%s'"
                            .formatted(context.currentPath(), idArg.name.value()));
                }
            }

            currentType = currentClass.name().asReference();
        } else {
            throw new AnalyzerException("Expression at '%s' is invalid: '%s' is not supported"
                    .formatted(context.currentPath(), primary));
        }

        for (int i = shift; i < idArgs.size(); i++) {
            AnalyzableClass analyzableClass = context.classes().get(currentType);

            IdArg idArg = idArgs.get(i);

            if (idArg.arguments == null) {
                AnalyzableField field = analyzableClass.getField(
                        context,
                        new AnalyzableField.Key(idArg.name),
                        "Expression");

                currentType = field.type();
            } else {
                AnalyzableMethod method = context.findMatchingExecutable(
                        analyzableClass,
                        idArg.arguments,
                        currentClass -> currentClass.methods().values()
                                .stream()
                                .filter(m -> m.name().equals(idArg.name))
                                .collect(Collectors.toList()),
                        AnalyzableMethod::parameters);

                if (method == null) {
                    List<ReferenceNode> argumentsTypes = context.argumentTypes(idArg.arguments);

                    throw new AnalyzerException("Expression at '%s' is invalid: reference to unknown method '%s(%s)' in type '%s'"
                            .formatted(
                                    context.currentPath(),
                                    idArg.name.value(),
                                    argumentsTypes.stream()
                                            .map(ReferenceNode::value)
                                            .collect(Collectors.joining(",")),
                                    currentType.value()));
                }

                if (!allowVoid) {
                    if (method.returnType() == null) {
                        throw new AnalyzerException("Expression at '%s' is invalid: reference to void method '%s(%s)' in type '%s'"
                                .formatted(
                                        context.currentPath(),
                                        idArg.name.value(),
                                        method.parameters().pars().stream()
                                                .map(par -> par.type().value())
                                                .collect(Collectors.joining(",")),
                                        currentType.value()));
                    }

                    currentType = method.returnType();
                } else {
                    currentType = new ReferenceNode("<void>");

                    if (i != idArgs.size() - 1) {
                        throw new AnalyzerException("Expression at '%s' is invalid: reference after call of void method '%s(%s)' in type '%s'"
                                .formatted(
                                        context.currentPath(),
                                        idArg.name.value(),
                                        method.parameters().pars().stream()
                                                .map(par -> par.type().value())
                                                .collect(Collectors.joining(",")),
                                        currentType.value()));
                    }
                }
            }
        }

        return currentType;
    }

    @NotNull
    public ReferenceNode compile(@NotNull CompilationContext context, @NotNull ClassNode currentClass,
                                 @NotNull ConstantPool constantPool, @NotNull CodeAttribute.VariablePool variablePool,
                                 @NotNull DataOutput dataOutput, boolean allowVoid) throws IOException {
        ReferenceNode currentType;

        int shift = 0;

        if (primary instanceof IntegerLiteralNode node) {
            // new (Integer)
            ClassConstant oClass = CompilationUtils.oClass(constantPool, "Integer");
            dataOutput.writeByte(Opcodes.NEW);
            dataOutput.writeShort(oClass.index());

            // dup
            dataOutput.writeByte(Opcodes.DUP);

            // iconst (#X)
            byte[] iconst = Opcodes.iconst(constantPool, node.value());
            dataOutput.write(iconst);

            context.incrementStackSize(3); // new + dup + fconst

            // invokevirtual (#Integer.<init>(int))
            MethodRefConstant oMethod = CompilationUtils.oMethod(constantPool,
                    "Integer", "<init>", "(I)V");
            dataOutput.writeByte(Opcodes.INVOKEVIRTUAL);
            dataOutput.writeShort(oMethod.index());

            context.decrementStackSize(2); // invokevirtual for this and int

            currentType = new ReferenceNode("Integer");
        } else if (primary instanceof RealLiteralNode node) {
            // new (Real)
            ClassConstant oClass = CompilationUtils.oClass(constantPool, "Real");
            dataOutput.writeByte(Opcodes.NEW);
            dataOutput.writeShort(oClass.index());

            // dup
            dataOutput.writeByte(Opcodes.DUP);

            // fconst (#X)
            dataOutput.write(Opcodes.fconst(constantPool, node.value()));

            context.incrementStackSize(3); // new + dup + fconst

            // invokevirtual (#Real.<init>(float))
            MethodRefConstant oMethod = CompilationUtils.oMethod(constantPool,
                    "Real", "<init>", "(F)V");
            dataOutput.writeByte(Opcodes.INVOKEVIRTUAL);
            dataOutput.writeShort(oMethod.index());

            context.decrementStackSize(2); // invokevirtual for this and float

            currentType = new ReferenceNode("Real");
        } else if (primary instanceof BooleanLiteralNode node) {
            // new (Boolean)
            ClassConstant oClass = CompilationUtils.oClass(constantPool, "Boolean");
            dataOutput.writeByte(Opcodes.NEW);
            dataOutput.writeShort(oClass.index());

            // dup
            dataOutput.writeByte(Opcodes.DUP);

            // iconst (#X)
            dataOutput.writeByte(node.value() ? Opcodes.ICONST_1 : Opcodes.ICONST_0);

            context.incrementStackSize(3); // new + dup + iconst

            // invokevirtual (#Boolean.<init>(boolean))
            MethodRefConstant oMethod = CompilationUtils.oMethod(constantPool,
                    "Boolean", "<init>", "(Z)V");
            dataOutput.writeByte(Opcodes.INVOKEVIRTUAL);
            dataOutput.writeShort(oMethod.index());

            context.decrementStackSize(2); // invokevirtual for this and boolean

            currentType = new ReferenceNode("Boolean");
        } else if (primary instanceof ReferenceNode referenceNode) {
            AnalyzableClass analyzableClass = context.analyzeContext().classes().get(referenceNode);
            if (analyzableClass != null) {
                if (idArgs.isEmpty()) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                IdArg idArg = idArgs.get(0);
                if (!idArg.name.value().equals("<init>") || idArg.arguments == null) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                AnalyzableConstructor constructor = context.analyzeContext().findMatchingExecutable(
                        analyzableClass,
                        idArg.arguments,
                        currentClass1 -> new ArrayList<>(currentClass1.constructors().values()),
                        AnalyzableConstructor::parameters);
                if (constructor == null) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                // new (#X)
                ClassConstant oClass = CompilationUtils.oClass(constantPool, analyzableClass.name().value());
                dataOutput.writeByte(Opcodes.NEW);
                dataOutput.writeShort(oClass.index());

                // dup
                dataOutput.writeByte(Opcodes.DUP);

                context.incrementStackSize(2); // new + dup

                // compile arguments
                for (ExpressionNode expressionNode : idArg.arguments.expressions()) {
                    expressionNode.compile(context, currentClass, constantPool, variablePool, dataOutput, false);
                }

                // invokevirtual (#X.<init>(X))
                MethodRefConstant oMethod = CompilationUtils.oMethod(constantPool, oClass.value().value(),
                        constructor.constructorNode());
                dataOutput.writeByte(Opcodes.INVOKEVIRTUAL);
                dataOutput.writeShort(oMethod.index());

                context.decrementStackSize(idArg.arguments.expressions().size() + 1); // invokevirtual for this and arguments


                shift = 1;

                currentType = analyzableClass.name().asReference();
            } else {
                if (!idArgs.isEmpty() && idArgs.get(0).name.value().equals("<init>")) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                // aload (#X)
                int index = variablePool.getIndex(referenceNode.value());
                dataOutput.write(Opcodes.aload(constantPool, index));

                context.incrementStackSize(1); // aload

                AnalyzableVariable variable = context.analyzeContext().variables().get(referenceNode);
                if (variable != null) {
                    currentType = variable.type();
                } else {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }
            }
        } else if (primary instanceof ThisNode) {
            // aload_0
            dataOutput.writeByte(Opcodes.ALOAD_0);

            context.incrementStackSize(1); // aload

            currentType = currentClass.name().asReference();
        } else {
            throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
        }

        // handle call chain
        for (int i = shift; i < idArgs.size(); i++) {
            AnalyzableClass analyzableClass = context.analyzeContext().classes().get(currentType);

            IdArg idArg = idArgs.get(i);

            if (idArg.arguments == null) {
                AnalyzableField field = analyzableClass.getField(
                        context.analyzeContext(),
                        new AnalyzableField.Key(idArg.name),
                        "Expression");

                // getfield (#X.X)
                FieldRefConstant oField = CompilationUtils.oField(constantPool, currentType.value(),
                        field.fieldNode());

                dataOutput.writeByte(Opcodes.GETFIELD);
                dataOutput.writeShort(oField.index());

                currentType = field.type();
            } else {
                AnalyzableMethod method = context.analyzeContext().findMatchingExecutable(
                        analyzableClass,
                        idArg.arguments,
                        currentClass1 -> currentClass1.methods().values()
                                .stream()
                                .filter(m -> m.name().equals(idArg.name))
                                .collect(Collectors.toList()),
                        AnalyzableMethod::parameters);

                if (method == null || (!allowVoid && method.returnType() == null)) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                // compile arguments
                for (ExpressionNode expressionNode : idArg.arguments.expressions()) {
                    expressionNode.compile(context, currentClass, constantPool, variablePool, dataOutput, false);
                }

                // invokevirtual (#X.x(X))
                MethodRefConstant oMethod = CompilationUtils.oMethod(constantPool, currentType.value(),
                        method.methodNode());

                dataOutput.writeByte(Opcodes.INVOKEVIRTUAL);
                dataOutput.writeShort(oMethod.index());

                // invokevirtual for this and arguments, but there can be return value
                context.decrementStackSize(method.parameters().pars().size());

                currentType = method.returnType();
                if (currentType == null) {
                    currentType = new ReferenceNode("<void>");

                    context.decrementStackSize(1); // no return from invokevirtual
                }
            }
        }

        return currentType;
    }

    public record IdArg(@NotNull IdentifierNode name, @Nullable ArgumentsNode arguments) {

    }
}
