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
import ru.team.compiler.compiler.attribute.CompilationExecutable;
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
import ru.team.compiler.tree.node.clas.ConstructorNode;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;
import ru.team.compiler.tree.node.primary.IntegerLiteralNode;
import ru.team.compiler.tree.node.primary.PrimaryNode;
import ru.team.compiler.tree.node.primary.RealLiteralNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.primary.SuperNode;
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
            } else if (primary instanceof SuperNode && iterator.lookup(TokenType.OPENING_PARENTHESIS)) {
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
                } else if (iterator.consume(TokenType.OPENING_BRACKET)) {
                    ReferenceNode referenceNode = ReferenceNode.PARSER.parse(iterator);

                    idArgs.add(new IdArg(
                            new IdentifierNode("<cast>"),
                            new ArgumentsNode(List.of(
                                    new ExpressionNode(referenceNode, List.of())))));

                    iterator.next(TokenType.CLOSING_BRACKET);
                } else if (iterator.consume(TokenType.INSTANCEOF_KEYWORD)) {
                    iterator.next(TokenType.OPENING_BRACKET);

                    ReferenceNode referenceNode = ReferenceNode.PARSER.parse(iterator);

                    idArgs.add(new IdArg(
                            new IdentifierNode("<instanceof>"),
                            new ArgumentsNode(List.of(
                                    new ExpressionNode(referenceNode, List.of())))));

                    iterator.next(TokenType.CLOSING_BRACKET);
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
        return type(context, allowVoid, true);
    }

    @NotNull
    public ReferenceNode type(@NotNull AnalyzeContext context, boolean allowVoid, boolean checkUninitialized) {
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

                AnalyzableConstructor constructor = analyzableClass.findMatchingConstructor(
                        context, idArg.arguments, checkUninitialized);

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

                if (checkUninitialized && !context.initializedVariables().contains(variable.name().asReference())) {
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

            if (context.currentConstructor() != null && !idArgs.isEmpty()) {
                IdArg idArg = idArgs.get(0);

                // TODO: maybe also forbid method calls if all fields are not initialized
                if (checkUninitialized && idArg.arguments == null
                        && !context.initializedFields().contains(idArg.name.asReference())) {
                    throw new AnalyzerException("Expression at '%s' is invalid: reference to uninitialized field '%s'"
                            .formatted(context.currentPath(), idArg.name.value()));
                }
            }

            currentType = currentClass.name().asReference();
        } else if (primary instanceof SuperNode) {
            AnalyzableClass currentClass = context.currentClass("Expression");

            currentType = currentClass.parentClass();

            if (idArgs.isEmpty()) {
                throw new AnalyzerException("Expression at '%s' is invalid: incomplete reference to super");
            }

            IdArg idArg = idArgs.get(0);
            if (idArg.arguments == null) {
                throw new AnalyzerException("Expression at '%s' is invalid: reference to field super.%s"
                        .formatted(context.currentPath(), idArg.name.value()));
            }

            if (idArg.name.value().equals("<init>")) {
                AnalyzableConstructor currentConstructor = context.currentConstructor();
                if (currentConstructor == null) {
                    throw new AnalyzerException("Expression at '%s' is invalid: reference to super constructor outside of the constructor context"
                            .formatted(context.currentPath()));
                }

                AnalyzableClass parentClass = currentClass.findParentClass(context, "Expression");
                if (parentClass == null) {
                    throw new AnalyzerException("Expression at '%s' is invalid: reference to super without any parents"
                            .formatted(context.currentPath()));
                }

                AnalyzableConstructor constructor = parentClass.findMatchingConstructor(
                        context, idArg.arguments, checkUninitialized);
                if (constructor == null) {
                    List<ReferenceNode> argumentsTypes = context.argumentTypes(idArg.arguments);

                    ConstructorNode constructorNode = currentConstructor.constructorNode();

                    if (constructorNode.syntheticSuperCall()) {
                        throw new AnalyzerException(
                                ("Constructor at '%s' is invalid: there is no super constructor " +
                                        "with no arguments, so it must be defined explicitly")
                                        .formatted(context.currentPath()));
                    }

                    throw new AnalyzerException("Expression at '%s' is invalid: reference to unknown super constructor '%s(%s)'"
                            .formatted(
                                    context.currentPath(),
                                    parentClass.name().value(),
                                    argumentsTypes.stream()
                                            .map(ReferenceNode::value)
                                            .collect(Collectors.joining(","))));
                }

                if (idArgs.size() != 1) {
                    List<ReferenceNode> argumentsTypes = context.argumentTypes(idArg.arguments);

                    throw new AnalyzerException("Expression at '%s' is invalid: reference after call of super constructor '(%s)'"
                            .formatted(
                                    context.currentPath(),
                                    argumentsTypes.stream()
                                            .map(ReferenceNode::value)
                                            .collect(Collectors.joining(","))));
                }

                currentType = new ReferenceNode("<void>");

                shift = 1;
            }
        } else {
            throw new AnalyzerException("Expression at '%s' is invalid: '%s' is not supported"
                    .formatted(context.currentPath(), primary));
        }

        for (int i = shift; i < idArgs.size(); i++) {
            AnalyzableClass analyzableClass = context.classes().get(currentType);

            IdArg idArg = idArgs.get(i);

            boolean cast = idArg.name.value().equals("<cast>");
            boolean instance = idArg.name.value().equals("<instanceof>");
            if (cast || instance) {
                if (idArg.arguments == null) {
                    throw new AnalyzerException("Expression at '%s' is invalid: %s without specified class"
                            .formatted(context.currentPath(), cast ? "cast" : "instanceof"));
                }

                List<ExpressionNode> expressions = idArg.arguments.expressions();
                if (expressions.size() != 1
                        || !(expressions.get(0).primary instanceof ReferenceNode requiredClass)
                        || !expressions.get(0).idArgs.isEmpty()) {
                    throw new AnalyzerException("Expression at '%s' is invalid: %s without correctly specified class"
                            .formatted(context.currentPath(), cast ? "cast" : "instanceof"));
                }

                if (!context.hasClass(requiredClass)) {
                    throw new AnalyzerException("Expression at '%s' is invalid: %s unknown class '%s'"
                            .formatted(context.currentPath(), cast ? "cast to" : "instanceof", currentType, requiredClass));
                }

                if (cast && !context.isAssignableFrom(currentType, requiredClass)
                        && !context.isAssignableFrom(requiredClass, currentType)) {
                    throw new AnalyzerException("Expression at '%s' is invalid: cast from '%s' to '%s' is not possible"
                            .formatted(context.currentPath(), currentType, requiredClass));
                }

                if (instance && (currentType.equals(requiredClass)
                        || !context.isAssignableFrom(currentType, requiredClass))) {
                    throw new AnalyzerException("Expression at '%s' is invalid: '%s' instanceof '%s' is always %s"
                            .formatted(
                                    context.currentPath(),
                                    currentType.value(),
                                    requiredClass.value(),
                                    context.isAssignableFrom(requiredClass, currentType)));
                }

                currentType = cast ? requiredClass : new ReferenceNode("Boolean");
            } else if (idArg.arguments == null) {
                AnalyzableField field = analyzableClass.getField(
                        context,
                        new AnalyzableField.Key(idArg.name),
                        "Expression");

                currentType = field.type();
            } else {
                AnalyzableMethod method = analyzableClass.findMatchingMethod(
                        context, idArg.name, idArg.arguments, checkUninitialized);

                if (method == null) {
                    boolean superCall = i == 0 && primary instanceof SuperNode;

                    List<ReferenceNode> argumentsTypes = context.argumentTypes(idArg.arguments);

                    throw new AnalyzerException("Expression at '%s' is invalid: reference to unknown %smethod '%s(%s)' in type '%s'"
                            .formatted(
                                    context.currentPath(),
                                    superCall ? "super " : "",
                                    idArg.name.value(),
                                    argumentsTypes.stream()
                                            .map(ReferenceNode::value)
                                            .collect(Collectors.joining(",")),
                                    superCall ? analyzableClass.parentClass().value() : currentType.value()));
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
                                 @NotNull CompilationExecutable currentExecutable,
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

                AnalyzableConstructor constructor = analyzableClass.findMatchingConstructor(
                        context.analyzeContext(), idArg.arguments, false);

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
                    expressionNode.compile(context, currentClass, constantPool, variablePool, currentExecutable, dataOutput, false);
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
                if (variable == null) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                currentType = variable.type();
            }
        } else if (primary instanceof ThisNode) {
            // aload_0
            dataOutput.writeByte(Opcodes.ALOAD_0);

            context.incrementStackSize(1); // aload

            currentType = currentClass.name().asReference();
        } else if (primary instanceof SuperNode) {
            if (idArgs.isEmpty()) {
                throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
            }

            IdArg idArg = idArgs.get(0);
            if (idArg.arguments == null) {
                throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
            }

            if (idArg.name.value().equals("<init>")) {
                if (!currentExecutable.isConstructor()) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                AnalyzableClass analyzableClass = context.analyzeContext().classes().get(currentClass.name().asReference());
                if (analyzableClass == null) {
                    throw new IllegalStateException("Got unknown class '%s'".formatted(currentClass.name().value()));
                }

                AnalyzableClass parentClass = analyzableClass.findParentClass(context.analyzeContext(), "Expression");
                if (parentClass == null) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                AnalyzableConstructor constructor = parentClass.findMatchingConstructor(
                        context.analyzeContext(), idArg.arguments, false);
                if (constructor == null) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                // aload_0
                dataOutput.writeByte(Opcodes.ALOAD_0);

                context.incrementStackSize(1); // aload

                // compile arguments
                for (ExpressionNode expressionNode : idArg.arguments.expressions()) {
                    expressionNode.compile(context, currentClass, constantPool, variablePool, currentExecutable, dataOutput, false);
                }

                MethodRefConstant oMethod = CompilationUtils.oMethod(constantPool, parentClass.name().value(),
                        constructor.constructorNode());

                // invokespecial (#X.<init>(X))
                dataOutput.writeByte(Opcodes.INVOKESPECIAL);
                dataOutput.writeShort(oMethod.index());

                context.decrementStackSize(idArg.arguments.expressions().size()); // invokevirtual for arguments

                shift = 1;

                currentType = new ReferenceNode("<void>");
            } else {
                // aload_0
                dataOutput.writeByte(Opcodes.ALOAD_0);

                context.incrementStackSize(1); // aload

                currentType = currentClass.parentName();
                if (currentType == null) {
                    currentType = new ReferenceNode("Any");
                }
            }
        } else {
            throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
        }

        // handle call chain
        for (int i = shift; i < idArgs.size(); i++) {
            AnalyzableClass analyzableClass = context.analyzeContext().classes().get(currentType);

            IdArg idArg = idArgs.get(i);

            boolean cast = idArg.name.value().equals("<cast>");
            boolean instance = idArg.name.value().equals("<instanceof>");
            if (cast || instance) {
                if (idArg.arguments == null) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                List<ExpressionNode> expressions = idArg.arguments.expressions();
                if (expressions.size() != 1
                        || !(expressions.get(0).primary instanceof ReferenceNode requiredClass)
                        || !expressions.get(0).idArgs.isEmpty()) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                if (cast && !context.analyzeContext().isAssignableFrom(currentType, requiredClass)
                        && !context.analyzeContext().isAssignableFrom(requiredClass, currentType)) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                if (instance && (currentType.equals(requiredClass)
                        || !context.analyzeContext().isAssignableFrom(currentType, requiredClass))) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                if (cast) {
                    // checkcast (#X)
                    ClassConstant oClass = CompilationUtils.oClass(constantPool, requiredClass.value());

                    dataOutput.writeByte(Opcodes.CHECKCAST);
                    dataOutput.writeShort(oClass.index());

                    currentType = requiredClass;
                } else {
                    // instanceof (#X)
                    ClassConstant oClass = CompilationUtils.oClass(constantPool, requiredClass.value());

                    dataOutput.writeByte(Opcodes.INSTANCEOF);
                    dataOutput.writeShort(oClass.index());

                    currentType = new ReferenceNode("Boolean");
                }
            } else if (idArg.arguments == null) {
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
                AnalyzableMethod method = analyzableClass.findMatchingMethod(
                        context.analyzeContext(), idArg.name, idArg.arguments, false);

                if (method == null || (!allowVoid && method.returnType() == null)) {
                    throw new IllegalStateException("ExpressionNode#compile called before ExpressionNode#analyze");
                }

                // compile arguments
                for (ExpressionNode expressionNode : idArg.arguments.expressions()) {
                    expressionNode.compile(context, currentClass, constantPool, variablePool, currentExecutable, dataOutput, false);
                }

                // invokevirtual (#X.x(X)) for generic call / invokespecial (#X.x(X)) for super call
                MethodRefConstant oMethod = CompilationUtils.oMethod(constantPool, currentType.value(),
                        method.methodNode());

                boolean superCall = i == 0 && primary instanceof SuperNode;
                if (superCall) {
                    dataOutput.writeByte(Opcodes.INVOKESPECIAL);
                } else {
                    dataOutput.writeByte(Opcodes.INVOKEVIRTUAL);
                }
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
