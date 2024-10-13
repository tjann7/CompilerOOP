package ru.team.compiler.tree.node;

import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.PrimaryNode;

public sealed abstract class TreeNode permits PrimaryNode, ExpressionNode, IdentifierNode, ArgumentsNode {

}
