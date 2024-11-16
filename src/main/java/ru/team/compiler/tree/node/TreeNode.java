package ru.team.compiler.tree.node;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.tree.node.clas.ClassMemberNode;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.IncludeNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.clas.ProgramNode;
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.PrimaryNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.StatementNode;

import java.util.List;

public abstract sealed class TreeNode permits PrimaryNode, IdentifierNode, ExpressionNode, ArgumentsNode,
                                              BodyNode, StatementNode, ParametersNode, ClassMemberNode,
                                              ClassNode, ProgramNode, IncludeNode {

    public static final TreeNodeParser<TreeNode> PARSER = new TreeNodeParser<>() {

        private List<TreeNodeParser<? extends TreeNode>> parsers;

        @Override
        @NotNull
        public TreeNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            for (TreeNodeParser<? extends TreeNode> parser : parsers()) {
                TokenIterator copiedIterator = iterator.copy();

                try {
                    TreeNode node = parser.parse(copiedIterator);
                    if (copiedIterator.hasNext()) {
                        continue;
                    }

                    return node;
                } catch (NodeFormatException e) {
                    continue;
                }
            }

            StringBuilder stringBuilder = new StringBuilder();
            Token firstToken = null;
            while (iterator.hasNext()) {
                Token token = iterator.next();
                stringBuilder.append(token.value()).append(" ");

                if (firstToken == null) {
                    firstToken = token;
                }
            }

            throw new NodeFormatException("meaningful tokens", stringBuilder.toString(), firstToken);
        }

        @NotNull
        private List<TreeNodeParser<? extends TreeNode>> parsers() {
            if (parsers == null) {
                parsers = List.of(
                        PrimaryNode.PARSER, IdentifierNode.PARSER, ExpressionNode.PARSER, ArgumentsNode.PARSER,
                        StatementNode.PARSER, ParametersNode.PARSER, ClassMemberNode.PARSER,
                        ClassNode.PARSER, ProgramNode.PARSER, IncludeNode.PARSER
                );
            }

            return parsers;
        }
    };

    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        return context;
    }

}
