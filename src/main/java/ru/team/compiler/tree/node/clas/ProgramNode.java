package ru.team.compiler.tree.node.clas;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class ProgramNode extends TreeNode {

    public static final TreeNodeParser<ProgramNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ProgramNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            List<ClassNode> classNodes = new ArrayList<>();

            while (iterator.hasNext()) {
                ClassNode classNode = ClassNode.PARSER.parse(iterator);
                classNodes.add(classNode);
            }

            return new ProgramNode(classNodes);
        }
    };

    private final List<ClassNode> classes;

    public ProgramNode(@NotNull List<ClassNode> classes) {
        this.classes = List.copyOf(classes);
    }

    @NotNull
    @Unmodifiable
    public List<ClassNode> classes() {
        return classes;
    }

    @Override
    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        AnalyzeContext initialContext = context;

        for (ClassNode classNode : classes) {
            context = classNode.analyze(context);
        }

        return initialContext;
    }

    @NotNull
    public ProgramNode optimize() {
        return new ProgramNode(classes.stream()
                .map(ClassNode::optimize)
                .collect(Collectors.toList()));
    }
}
