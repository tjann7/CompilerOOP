package ru.team.compiler.tree.node.clas;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.Token;
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
            List<IncludeNode> includeNodes = new ArrayList<>();
            List<ClassNode> classNodes = new ArrayList<>();

            while (iterator.hasNext()) {
                Token token = iterator.lookup();

                switch (token.type()) {
                    case CLASS_KEYWORD -> {
                        ClassNode classNode = ClassNode.PARSER.parse(iterator);
                        classNodes.add(classNode);
                    }
                    case INCLUDE_KEYWORD -> {
                        IncludeNode includeNode = IncludeNode.PARSER.parse(iterator);
                        includeNodes.add(includeNode);
                    }
                    default -> throw new NodeFormatException("class/include", token);
                }
            }

            return new ProgramNode(includeNodes, classNodes);
        }
    };

    private final List<IncludeNode> includeNodes;
    private final List<ClassNode> classes;

    public ProgramNode(@NotNull List<IncludeNode> includeNodes, @NotNull List<ClassNode> classes) {
        this.includeNodes = List.copyOf(includeNodes);
        this.classes = List.copyOf(classes);
    }

    @NotNull
    @Unmodifiable
    public List<IncludeNode> includeNodes() {
        return includeNodes;
    }

    @NotNull
    @Unmodifiable
    public List<ClassNode> classes() {
        return classes;
    }

    @Override
    @NotNull
    public AnalyzeContext analyzeUnsafe(@NotNull AnalyzeContext context) {
        for (ClassNode classNode : classes) {
            context = classNode.analyze(context);
        }

        return context;
    }

    @NotNull
    public ProgramNode optimize() {
        return new ProgramNode(
                includeNodes,
                classes.stream()
                        .map(ClassNode::optimize)
                        .collect(Collectors.toList())
        );
    }
}
