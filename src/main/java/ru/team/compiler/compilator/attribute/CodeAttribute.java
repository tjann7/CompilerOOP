package ru.team.compiler.compilator.attribute;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzableVariable;
import ru.team.compiler.compilator.CompilationContext;
import ru.team.compiler.compilator.constant.ConstantPool;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.ConstructorNode;
import ru.team.compiler.tree.node.clas.MethodNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.StatementNode;
import ru.team.compiler.tree.node.statement.VariableDeclarationNode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CodeAttribute extends Attribute {

    private final ClassNode classNode;
    private final ParametersNode parametersNode;
    private final BodyNode bodyNode;

    public CodeAttribute(@NotNull ConstantPool constantPool, @NotNull ClassNode classNode,
                         @NotNull ParametersNode parametersNode, @NotNull BodyNode bodyNode) {
        super(constantPool.getUtf("Code"));

        this.classNode = classNode;
        this.parametersNode = parametersNode;
        this.bodyNode = bodyNode;
    }

    public CodeAttribute(@NotNull ConstantPool constantPool, @NotNull ClassNode classNode,
                         @NotNull MethodNode methodNode) {
        this(constantPool, classNode, methodNode.parameters(), methodNode.body());
    }

    public CodeAttribute(@NotNull ConstantPool constantPool, @NotNull ClassNode classNode,
                         @NotNull ConstructorNode constructorNode) {
        this(constantPool, classNode, constructorNode.parameters(), constructorNode.body());
    }

    @Override
    public void compile(@NotNull CompilationContext context, @NotNull ConstantPool constantPool,
                        @NotNull DataOutput dataOutput) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        DataOutput byteDataOutput = new DataOutputStream(byteArrayOutputStream);

        // TODO: calculate real max stack
        int maxStack = 100;

        List<AnalyzableVariable> locals = new ArrayList<>();

        locals.addAll(parametersNode.pars().stream()
                .map(par -> new AnalyzableVariable(par.name(), par.type()))
                .collect(Collectors.toList()));

        locals.addAll(bodyNode.flatStatements().stream()
                .filter(value -> value instanceof VariableDeclarationNode)
                .map(value -> (VariableDeclarationNode) value)
                .map(value -> new AnalyzableVariable(value.name(), value.type()))
                .collect(Collectors.toList()));

        VariablePool variablePool = new VariablePool();
        Map<ReferenceNode, AnalyzableVariable> variables = new HashMap<>();

        for (AnalyzableVariable local : locals) {
            variablePool.add(local.name().value());
            variables.put(local.name().asReference(), local);
        }

        int maxLocals = 1 + locals.size();

        byteDataOutput.writeShort(maxStack);
        byteDataOutput.writeShort(maxLocals);

        // Code
        context = new CompilationContext(context.analyzeContext()
                .withClass(classNode)
                .withVariables(variables));
        byte[] bytes = compileBodyNode(context, constantPool, variablePool);
        byteDataOutput.writeInt(bytes.length);
        byteDataOutput.write(bytes);

        // Exceptions
        byteDataOutput.writeShort(0);

        // Attributes
        byteDataOutput.writeShort(0);

        // ---

        dataOutput.writeShort(attributeName.index());

        dataOutput.writeInt(byteArrayOutputStream.size());
        dataOutput.write(byteArrayOutputStream.toByteArray());
    }

    private byte @NotNull [] compileBodyNode(@NotNull CompilationContext context, @NotNull ConstantPool constantPool,
                                             @NotNull CodeAttribute.VariablePool variablePool) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        DataOutput byteDataOutput = new DataOutputStream(byteArrayOutputStream);

        for (StatementNode statementNode : bodyNode.statements()) {
            statementNode.compile(context, classNode, constantPool, variablePool, byteDataOutput);
        }

        return byteArrayOutputStream.toByteArray();
    }

    public static final class VariablePool {

        private final List<String> variables = new ArrayList<>();
        private final Map<String, Integer> variableToIndex = new HashMap<>();

        public void add(@NotNull String variable) {
            variableToIndex.computeIfAbsent(variable, k -> {
                int index = variables.size() + 1;
                variables.add(variable);
                return index;
            });
        }

        public int getIndex(@NotNull IdentifierNode node) {
            return getIndex(node.value());
        }

        public int getIndex(@NotNull ReferenceNode node) {
            return getIndex(node.value());
        }

        public int getIndex(@NotNull String variable) {
            Integer index = variableToIndex.get(variable);
            if (index == null) {
                throw new IllegalArgumentException("Unknown variable: " + variable);
            }

            return index;
        }

    }
}
