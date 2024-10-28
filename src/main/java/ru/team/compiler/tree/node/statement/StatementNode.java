package ru.team.compiler.tree.node.statement;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;

public abstract sealed class StatementNode extends TreeNode permits AssignmentNode, WhileLoopNode, IfNode, ReturnNode,
                                                                    VariableDeclarationNode, MethodCallNode {

    public static final TreeNodeParser<StatementNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public StatementNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            Token token = iterator.lookup();

            return switch (token.type()) {
                case IDENTIFIER, THIS_KEYWORD -> {
                    int index = iterator.index();

                    try {
                        yield AssignmentNode.PARSER.parse(iterator);
                    } catch (NodeFormatException e) {
                        if (token.type() == TokenType.IDENTIFIER) {
                            iterator.index(index);
                            yield MethodCallNode.PARSER.parse(iterator);
                        } else {
                            throw e;
                        }
                    }
                }
                case WHILE_KEYWORD -> WhileLoopNode.PARSER.parse(iterator);
                case IF_KEYWORD -> IfNode.PARSER.parse(iterator);
                case RETURN_KEYWORD -> ReturnNode.PARSER.parse(iterator);
                case VAR_KEYWORD -> VariableDeclarationNode.PARSER.parse(iterator);
                default -> throw new NodeFormatException("assignment/while/if/return/var", token);
            };
        }
    };
}
