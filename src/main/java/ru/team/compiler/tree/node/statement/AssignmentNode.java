package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzableClass;
import ru.team.compiler.analyzer.AnalyzableField;
import ru.team.compiler.analyzer.AnalyzableVariable;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.compiler.CompilationContext;
import ru.team.compiler.compiler.CompilationUtils;
import ru.team.compiler.compiler.attribute.CodeAttribute;
import ru.team.compiler.compiler.attribute.CompilationExecutable;
import ru.team.compiler.compiler.constant.ConstantPool;
import ru.team.compiler.compiler.constant.FieldRefConstant;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.util.Opcodes;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class AssignmentNode extends StatementNode {

    public static final TreeNodeParser<AssignmentNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public AssignmentNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            boolean local = !iterator.consume(TokenType.THIS_KEYWORD);
            if (!local) {
                iterator.next(TokenType.DOT);
            }

            ReferenceNode referenceNode = ReferenceNode.PARSER.parse(iterator);

            iterator.next(TokenType.ASSIGNMENT_OPERATOR);

            ExpressionNode valueExpressionNode = ExpressionNode.PARSER.parse(iterator);
            return new AssignmentNode(local, referenceNode, valueExpressionNode);
        }
    };

    private final boolean local;
    private final ReferenceNode referenceNode;
    private final ExpressionNode valueExpression;

    public AssignmentNode(boolean local, @NotNull ReferenceNode referenceNode,
                          @NotNull ExpressionNode valueExpression) {
        this.local = local;
        this.referenceNode = referenceNode;
        this.valueExpression = valueExpression;
    }

    @NotNull
    public ReferenceNode referenceNode() {
        return referenceNode;
    }

    @NotNull
    public ExpressionNode valueExpression() {
        return valueExpression;
    }

    @Override
    @NotNull
    public AnalyzeContext analyzeUnsafe(@NotNull AnalyzeContext context) {
        ReferenceNode leftType;

        List<Exception> exceptions = new ArrayList<>();

        if (local) {
            AnalyzableVariable variable = context.variables().get(referenceNode);
            if (variable == null) {
                exceptions.add(new AnalyzerException("Assignment at '%s' is invalid: reference to unknown variable '%s'"
                        .formatted(context.currentPath(), referenceNode.value())));

                leftType = null;
            } else {
                leftType = variable.type();
            }
        } else {
            AnalyzableClass currentClass = context.currentClass("Assignment");

            try {
                AnalyzableField field = currentClass.getField(
                        context,
                        new AnalyzableField.Key(referenceNode.asIdentifier()),
                        "Assignment");

                leftType = field.type();
            } catch (AnalyzerException e) {
                exceptions.add(new AnalyzerException("Assignment at '%s' is invalid: reference to unknown field '%s'"
                        .formatted(context.currentPath(), referenceNode.value())));

                leftType = null;
            }
        }

        ReferenceNode rightType;
        try {
            rightType = valueExpression.type(context, false);
        } catch (AnalyzerException e) {
            exceptions.add(e);
            rightType = null;
        }

        if (leftType != null && rightType != null && !context.isAssignableFrom(leftType, rightType)) {
            throw new AnalyzerException("Assignment at '%s' is invalid: expected '%s' type on the right side, got '%s'"
                    .formatted(context.currentPath(), leftType.value(), rightType.value()));
        }

        if (local) {
            Set<ReferenceNode> initializedVariables = new HashSet<>(context.initializedVariables());
            initializedVariables.add(referenceNode);
            return context.withInitializedVariables(initializedVariables).addExceptions(exceptions);
        } else {
            Set<ReferenceNode> initializedFields = new HashSet<>(context.initializedFields());
            initializedFields.add(referenceNode);
            return context.withInitializedFields(initializedFields).addExceptions(exceptions);
        }
    }

    @Override
    public void compile(@NotNull CompilationContext context, @NotNull ClassNode currentClass,
                        @NotNull ConstantPool constantPool, @NotNull CodeAttribute.VariablePool variablePool,
                        @NotNull CompilationExecutable currentExecutable, @NotNull DataOutput dataOutput) throws IOException {
        if (!local) {
            // aload_0 (this)
            dataOutput.writeByte(Opcodes.ALOAD_0);

            context.incrementStackSize(1); // aload
        }

        valueExpression.compile(context, currentClass, constantPool, variablePool, currentExecutable, dataOutput, false);

        if (!local) {
            // putfield (#X)
            AnalyzableClass analyzableClass = context.analyzeContext().classes().get(currentClass.name().asReference());

            AnalyzableField field = analyzableClass.getField(
                    context.analyzeContext(),
                    new AnalyzableField.Key(new IdentifierNode(referenceNode.value())),
                    "Assignment");

            FieldRefConstant oField = CompilationUtils.oField(constantPool, currentClass.name().value(),
                    field.fieldNode());

            dataOutput.writeByte(Opcodes.PUTFIELD);
            dataOutput.writeShort(oField.index());

            context.decrementStackSize(1); // putfield
        } else {
            // astore (#X)
            int index = variablePool.getIndex(referenceNode.value());
            dataOutput.write(Opcodes.astore(constantPool, index));

            context.decrementStackSize(1); // astore
        }
    }
}
