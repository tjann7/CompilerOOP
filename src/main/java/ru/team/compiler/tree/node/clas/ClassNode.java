package ru.team.compiler.tree.node.clas;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class ClassNode extends TreeNode {

    public static final TreeNodeParser<ClassNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ClassNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.CLASS_KEYWORD);

            IdentifierNode identifierNode = IdentifierNode.PARSER.parse(iterator);

            ReferenceNode parentIdentifierNode;
            if (iterator.consume(TokenType.EXTENDS_KEYWORD)) {
                parentIdentifierNode = ReferenceNode.PARSER.parse(iterator);
            } else {
                parentIdentifierNode = null;
            }

            iterator.next(TokenType.IS_KEYWORD);

            List<ClassMemberNode> classMemberNodes = new ArrayList<>();

            while (iterator.hasNext()) {
                if (iterator.consume(TokenType.END_KEYWORD)) {
                    return new ClassNode(identifierNode, parentIdentifierNode, classMemberNodes);
                }

                ClassMemberNode classMemberNode = ClassMemberNode.PARSER.parse(iterator);
                classMemberNodes.add(classMemberNode);
            }

            throw new NodeFormatException("end", NodeFormatException.END_OF_STRING, iterator.lastToken());
        }
    };

    private final IdentifierNode name;
    private final ReferenceNode parentName;
    private final List<ClassMemberNode> classMembers;

    public ClassNode(@NotNull IdentifierNode name, @Nullable ReferenceNode parentName,
                     @NotNull List<ClassMemberNode> classMembers) {
        this.name = name;
        this.parentName = parentName;
        this.classMembers = List.copyOf(classMembers);
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

        return initialContext;
    }

    @NotNull
    public ClassNode optimize() {
        return new ClassNode(
                name,
                parentName,
                classMembers.stream()
                        .map(ClassMemberNode::optimize)
                        .collect(Collectors.toList())
        );
    }
}
