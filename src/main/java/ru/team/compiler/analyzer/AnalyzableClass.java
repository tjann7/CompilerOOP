package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            if (name().equals(currentClass.name())) {
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

    @Nullable
    public AnalyzableConstructor findMatchingConstructor(@NotNull AnalyzeContext context,
                                                         @NotNull ArgumentsNode arguments,
                                                         boolean checkInitialized) {
        return findMatchingExecutable(
                context,
                arguments,
                currentClass -> new ArrayList<>(currentClass.constructors().values()),
                AnalyzableConstructor::parameters,
                false,
                checkInitialized);
    }

    @Nullable
    public AnalyzableMethod findMatchingMethod(@NotNull AnalyzeContext context, @NotNull IdentifierNode name,
                                               @NotNull ArgumentsNode arguments, boolean checkInitialized) {
        return findMatchingExecutable(
                context,
                arguments,
                currentClass -> currentClass.methods().values()
                        .stream()
                        .filter(m -> m.name().equals(name))
                        .collect(Collectors.toList()),
                AnalyzableMethod::parameters,
                true,
                checkInitialized);
    }

    @Nullable
    private <E> E findMatchingExecutable(@NotNull AnalyzeContext context, @NotNull ArgumentsNode arguments,
                                         @NotNull Function<AnalyzableClass, List<E>> entitiesFromClass,
                                         @NotNull Function<E, ParametersNode> entityParameters,
                                         boolean lookupParent, boolean checkInitialized) {
        List<ReferenceNode> argumentsTypes = context.argumentTypes(arguments, checkInitialized);

        E finalEntity = null;

        AnalyzableClass currentClass = this;
        while (true) {
            List<E> entities = entitiesFromClass.apply(currentClass);

            for (E entity : entities) {
                ParametersNode parameters = entityParameters.apply(entity);
                int size = arguments.expressions().size();
                if (size != parameters.pars().size()) {
                    continue;
                }

                finalEntity = entity;

                for (int j = 0; j < size; j++) {
                    ReferenceNode argumentType = argumentsTypes.get(j);
                    ReferenceNode parameterType = parameters.pars().get(j).type();

                    if (!context.isAssignableFrom(parameterType, argumentType)) {
                        finalEntity = null;
                        break;
                    }
                }

                if (finalEntity != null) {
                    break;
                }
            }

            if (!lookupParent || finalEntity != null) {
                break;
            }

            currentClass = currentClass.findParentClass(context, "Expression");
            if (currentClass == null) {
                break;
            }
        }

        return finalEntity;
    }


}
