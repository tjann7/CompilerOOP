package ru.team.compiler.tree.node.clas;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;

import java.util.ArrayList;
import java.util.List;

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
    public AnalyzeContext traverse(@NotNull AnalyzeContext context) {
        AnalyzeContext initialContext = context;

        for (ClassNode classNode : classes) {
            context = classNode.traverse(context);
        }

        return initialContext;
    }
}
