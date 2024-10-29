package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
                throw new AnalyzerException("Expression at '%s' is invalid: class '%s' extends unknown '%s'"
                        .formatted(context.currentPath(), currentClass.name().value(),
                                currentClass.parentClass().value()));
            }

            currentClass = parentClass;
        }
    }

    @Nullable
    public AnalyzableClass findParentClass(@NotNull AnalyzeContext context, @NotNull String messagePrefix) {
        String name = name().value();
        if (name.equals("Any") || name.equals("")) {
            return null;
        }

        AnalyzableClass parentClass = context.classes().get(parentClass());
        if (parentClass == null) {
            throw new AnalyzerException("%s at '%s' is invalid: class '%s' extends unknown '%s'"
                    .formatted(messagePrefix, context.currentPath(), name().value(), parentClass().value()));
        }

        return parentClass;
    }

    @NotNull
    public AnalyzableField getField(@NotNull AnalyzeContext context, @NotNull AnalyzableField.Key key,
                                    @NotNull String messagePrefix) {
        AnalyzableField field;

        AnalyzableClass currentClass = this;
        while (true) {
            field = fields().get(key);
            if (field != null) {
                break;
            }

            currentClass = currentClass.findParentClass(context, messagePrefix);
            if (currentClass == null) {
                break;
            }
        }

        if (field == null) {
            throw new AnalyzerException("%s at '%s' is invalid: reference to unknown field '%s' in type '%s'"
                    .formatted(messagePrefix, context.currentPath(), key.name().value(), name().value()));
        }

        return field;
    }
}
