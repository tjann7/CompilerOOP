package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.Collections;
import java.util.Map;

public record AnalyzableClass(@NotNull IdentifierNode name,
                              @NotNull ReferenceNode parentClass,
                              @NotNull Map<AnalyzableConstructor.Key, AnalyzableConstructor> constructors,
                              @NotNull Map<AnalyzableMethod.Key, AnalyzableMethod> methods,
                              @NotNull Map<AnalyzableField.Key, AnalyzableField> fields) {

    public AnalyzableClass(@NotNull IdentifierNode name,
                           @NotNull ReferenceNode parentClass,
                           @NotNull Map<AnalyzableConstructor.Key, AnalyzableConstructor> constructors,
                           @NotNull Map<AnalyzableMethod.Key, AnalyzableMethod> methods,
                           @NotNull Map<AnalyzableField.Key, AnalyzableField> fields) {
        this.name = name;
        this.parentClass = parentClass;
        this.constructors = Collections.unmodifiableMap(constructors);
        this.methods = Collections.unmodifiableMap(methods);
        this.fields = Collections.unmodifiableMap(fields);
    }

    public boolean isAssignableFrom(@NotNull AnalyzeContext context, @NotNull AnalyzableClass other) {
        AnalyzableClass currentClass = other;

        while (true) {
            if (this.equals(currentClass)) {
                return true;
            } else if (currentClass.parentClass.value().equals("")) {
                return false;
            }

            AnalyzableClass parentClass = context.classes().get(currentClass.parentClass());
            if (parentClass == null) {
                throw new AnalyzerException("Expression in '%s' is invalid: class '%s' extends unknown '%s'"
                        .formatted(context.currentPath(), currentClass.name().value(),
                                currentClass.parentClass().value()));
            }

            currentClass = parentClass;
        }
    }
}
