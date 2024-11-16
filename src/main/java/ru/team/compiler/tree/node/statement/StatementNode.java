package ru.team.compiler.tree.node.statement;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.compilator.CompilationContext;
import ru.team.compiler.compilator.attribute.CodeAttribute;
import ru.team.compiler.compilator.constant.ConstantPool;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.clas.ClassNode;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

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
                        iterator.index(index);
                        yield MethodCallNode.PARSER.parse(iterator);
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

    public boolean alwaysReturn() {
        return false;
    }

    @NotNull
    public List<StatementNode> optimize() {
        return List.of(this);
    }

    public abstract void compile(@NotNull CompilationContext context, @NotNull ClassNode currentClass,
                                 @NotNull ConstantPool constantPool, @NotNull CodeAttribute.VariablePool variablePool,
                                 @NotNull DataOutput dataOutput) throws IOException;

}
