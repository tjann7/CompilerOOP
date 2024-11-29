package ru.team.compiler.tree.node.clas;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.team.compiler.analyzer.AnalyzableClass;
import ru.team.compiler.analyzer.AnalyzableMethod;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.primary.SuperNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.MethodCallNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class ClassNode extends TreeNode {

    public static final TreeNodeParser<ClassNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ClassNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.CLASS_KEYWORD);

            boolean isAbstract = iterator.consume(TokenType.ABSTRACT_KEYWORD);

            IdentifierNode identifierNode = IdentifierNode.PARSER.parse(iterator);

            ReferenceNode parentIdentifierNode;
            if (iterator.consume(TokenType.EXTENDS_KEYWORD)) {
                parentIdentifierNode = ReferenceNode.PARSER.parse(iterator);
            } else {
                if (identifierNode.value().equals("Any")) {
                    parentIdentifierNode = new ReferenceNode("");
                } else {
                    parentIdentifierNode = null;
                }
            }

            iterator.next(TokenType.IS_KEYWORD);

            List<ClassMemberNode> classMemberNodes = new ArrayList<>();
            boolean hasConstructor = false;

            while (iterator.hasNext()) {
                while (iterator.consume(TokenType.SEMICOLON)) {
                }

                if (iterator.consume(TokenType.END_KEYWORD)) {
                    if (!hasConstructor) {
                        classMemberNodes.add(
                                new ConstructorNode(
                                        false,
                                        new ParametersNode(List.of()),
                                        new BodyNode(List.of(
                                                new MethodCallNode(
                                                        new ExpressionNode(
                                                                new SuperNode(), List.of(
                                                                new ExpressionNode.IdArg(
                                                                        new IdentifierNode("<init>"),
                                                                        new ArgumentsNode(List.of()))))))),
                                        true));
                    }

                    return new ClassNode(isAbstract, identifierNode, parentIdentifierNode, classMemberNodes);
                }

                ClassMemberNode classMemberNode = ClassMemberNode.PARSER.parse(iterator);
                classMemberNodes.add(classMemberNode);

                if (classMemberNode instanceof ConstructorNode) {
                    hasConstructor = true;
                }
            }

            throw new NodeFormatException("end", NodeFormatException.END_OF_STRING, iterator.lastToken());
        }
    };

    private final boolean isAbstract;
    private final IdentifierNode name;
    private final ReferenceNode parentName;
    private final List<ClassMemberNode> classMembers;

    public ClassNode(boolean isAbstract, @NotNull IdentifierNode name, @Nullable ReferenceNode parentName,
                     @NotNull List<ClassMemberNode> classMembers) {
        this.isAbstract = isAbstract;
        this.name = name;
        this.parentName = parentName;
        this.classMembers = List.copyOf(classMembers);
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    @NotNull
    public IdentifierNode name() {
        return name;
    }

    @Nullable
    public ReferenceNode parentName() {
        return parentName;
    }

    @NotNull
    @Unmodifiable
    public List<ClassMemberNode> classMembers() {
        return classMembers;
    }

    @Override
    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        AnalyzeContext initialContext = context;

        context = context.withClass(this);
        for (ClassMemberNode classMemberNode : classMembers) {
            context = classMemberNode.analyze(context);
        }

        AnalyzableClass currentClass = Objects.requireNonNull(context.currentClass());
        if (!isAbstract) {
            Map<AnalyzableMethod.Key, AnalyzableClass> abstractMethods = new HashMap<>();
            Map<AnalyzableMethod.Key, AnalyzableMethod> nonAbstractMethods = new HashMap<>(currentClass.methods());

            AnalyzableClass currentParentClass = currentClass;
            while (true) {
                currentParentClass = currentParentClass.findParentClass(context, "Class");
                if (currentParentClass == null || !currentParentClass.classNode().isAbstract()) {
                    break;
                }

                for (AnalyzableMethod method : currentParentClass.methods().values()) {
                    if (method.methodNode().isAbstract()) {
                        abstractMethods.put(method.key(), currentParentClass);
                    } else {
                        nonAbstractMethods.putIfAbsent(method.key(), method);
                    }
                }
            }

            for (var entry : abstractMethods.entrySet()) {
                AnalyzableMethod.Key key = entry.getKey();

                AnalyzableMethod method = nonAbstractMethods.get(key);
                if (method == null) {
                    AnalyzableClass definedClass = entry.getValue();
                    throw new AnalyzerException("Class '%s' must implement abstract method '%s.%s(%s)'"
                            .formatted(name.value(), definedClass.classNode().name.value(),
                                    key.name().value(), key.parameterTypesAsString()));
                }
            }

        }

        return initialContext;
    }

    @NotNull
    public ClassNode optimize() {
        return new ClassNode(
                isAbstract,
                name,
                parentName,
                classMembers.stream()
                        .map(ClassMemberNode::optimize)
                        .collect(Collectors.toList())
        );
    }
}
