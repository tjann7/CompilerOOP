package ru.team.compiler.tree.node;

import ru.team.compiler.tree.node.clas.ClassMemberNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.PrimaryNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.StatementNode;

public abstract sealed class TreeNode permits PrimaryNode, IdentifierNode, ExpressionNode, ArgumentsNode,
                                              BodyNode, StatementNode, ParametersNode, ClassMemberNode {

}
