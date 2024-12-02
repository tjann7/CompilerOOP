package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.ConstructorNode;
import ru.team.compiler.tree.node.clas.MethodNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record AnalyzeContext(@NotNull Map<ReferenceNode, AnalyzableClass> classes,
                             @NotNull Map<ReferenceNode, AnalyzableVariable> variables,
                             @NotNull Set<ReferenceNode> initializedVariables,
                             @NotNull Set<ReferenceNode> initializedFields,
                             @NotNull List<Exception> exceptions,
                             @NotNull String currentPath,
                             @Nullable AnalyzableClass currentClass,
                             @Nullable AnalyzableMethod currentMethod,
                             @Nullable AnalyzableConstructor currentConstructor) {

    public AnalyzeContext(@NotNull Map<ReferenceNode, AnalyzableClass> classes,
                          @NotNull Map<ReferenceNode, AnalyzableVariable> variables,
                          @NotNull Set<ReferenceNode> initializedVariables,
                          @NotNull Set<ReferenceNode> initializedFields,
                          @NotNull List<Exception> exceptions,
                          @NotNull String currentPath,
                          @Nullable AnalyzableClass currentClass,
                          @Nullable AnalyzableMethod currentMethod,
                          @Nullable AnalyzableConstructor currentConstructor) {
        if (currentMethod != null && currentConstructor != null) {
            throw new IllegalArgumentException("Cannot create context with both not-null current method and constructor");
        }

        this.classes = Collections.unmodifiableMap(classes);
        this.variables = Collections.unmodifiableMap(variables);
        this.initializedVariables = Collections.unmodifiableSet(initializedVariables);
        this.initializedFields = Collections.unmodifiableSet(initializedFields);
        this.exceptions = Collections.unmodifiableList(exceptions);
        this.currentPath = currentPath;
        this.currentClass = currentClass;
        this.currentMethod = currentMethod;
        this.currentConstructor = currentConstructor;
    }

    public boolean hasClass(@NotNull ReferenceNode className) {
        return classes.containsKey(className);
    }

    public boolean hasVariable(@NotNull ReferenceNode variableName) {
        return variables.containsKey(variableName);
    }

    @NotNull
    public AnalyzeContext concatPath(@NotNull String path) {
        return new AnalyzeContext(
                classes, variables, initializedVariables, initializedFields, exceptions,
                currentPath.isEmpty() ? path : currentPath + "." + path, currentClass,
                currentMethod, currentConstructor
        );
    }

    @NotNull
    public AnalyzeContext withClass(@NotNull ClassNode classNode) {
        AnalyzableClass analyzableClass = classes.get(classNode.name().asReference());
        if (analyzableClass == null) {
            new AnalyzerException("Reference to unknown class '%s'".formatted(classNode.name().value()));
        }

        String path = classNode.name().value();
        return new AnalyzeContext(
                classes, variables, initializedVariables, initializedFields, exceptions,
                currentPath.isEmpty() ? path : currentPath + "." + path, analyzableClass,
                currentMethod, currentConstructor
        );
    }

    @NotNull
    public AnalyzeContext withMethod(@NotNull MethodNode method) {
        AnalyzableMethod.Key key = AnalyzableMethod.Key.fromNode(method);

        if (currentClass == null) {
            throw new AnalyzerException("'%s' is invalid: outside of the class context"
                    .formatted(currentPath()));
        }

        AnalyzableMethod analyzableMethod = currentClass.methods().get(key);
        if (analyzableMethod == null) {
            throw new AnalyzerException("Reference to unknown method '%s.%s(%s)'"
                    .formatted(currentClass.name().value(), method.name().value(), key.parameterTypesAsString()));
        }

        String path = analyzableMethod.name().value() + "(" + key.parameterTypesAsString() + ")";
        return new AnalyzeContext(
                classes, variables, initializedVariables, initializedFields, exceptions,
                currentPath.isEmpty() ? path : currentPath + "." + path, currentClass,
                analyzableMethod, null
        );
    }

    @NotNull
    public AnalyzeContext withConstructor(@NotNull ConstructorNode constructorNode) {
        AnalyzableConstructor.Key key = new AnalyzableConstructor.Key(constructorNode.parameters().pars().stream()
                .map(ParametersNode.Par::type)
                .collect(Collectors.toList()));

        if (currentClass == null) {
            throw new AnalyzerException("'%s' is invalid: outside of the class context"
                    .formatted(currentPath()));
        }

        AnalyzableConstructor analyzableConstructor = currentClass.constructors().get(key);
        if (analyzableConstructor == null) {
            throw new AnalyzerException("Reference to unknown constructor '%s(%s)'"
                    .formatted(currentClass.name().value(), key.parameterTypesAsString()));
        }

        String path = "this(" + key.parameterTypesAsString() + ")";
        return new AnalyzeContext(
                classes, variables, initializedVariables, initializedFields, exceptions,
                currentPath.isEmpty() ? path : currentPath + "." + path, currentClass,
                null, new AnalyzableConstructor(constructorNode, constructorNode.parameters(), currentClass)
        );
    }

    @NotNull
    public AnalyzeContext withVariables(@NotNull Map<ReferenceNode, AnalyzableVariable> variables) {
        return new AnalyzeContext(
                classes,
                variables,
                initializedVariables,
                initializedFields,
                exceptions,
                currentPath,
                currentClass,
                currentMethod,
                currentConstructor
        );
    }

    @NotNull
    public AnalyzeContext withInitializedVariables(@NotNull Set<ReferenceNode> initializedVariables) {
        return new AnalyzeContext(
                classes,
                variables,
                initializedVariables,
                initializedFields,
                exceptions,
                currentPath,
                currentClass,
                currentMethod,
                currentConstructor
        );
    }

    @NotNull
    public AnalyzeContext withInitializedFields(@NotNull Set<ReferenceNode> initializedFields) {
        return new AnalyzeContext(
                classes,
                variables,
                initializedVariables,
                initializedFields,
                exceptions,
                currentPath,
                currentClass,
                currentMethod,
                currentConstructor
        );
    }

    @NotNull
    public AnalyzeContext withExceptions(@NotNull List<Exception> exceptions) {
        return new AnalyzeContext(
                classes,
                variables,
                initializedVariables,
                initializedFields,
                exceptions,
                currentPath,
                currentClass,
                currentMethod,
                currentConstructor
        );
    }

    @NotNull
    public AnalyzeContext addException(@NotNull Exception exception) {
        List<Exception> newExceptions = new ArrayList<>(exceptions);
        newExceptions.add(exception);
        return withExceptions(newExceptions);
    }

    @NotNull
    public AnalyzeContext addExceptions(@NotNull List<Exception> exceptions) {
        List<Exception> newExceptions = new ArrayList<>(this.exceptions);
        newExceptions.addAll(exceptions);
        return withExceptions(newExceptions);
    }

    public boolean isAssignableFrom(@NotNull ReferenceNode requiredClassName, @NotNull ReferenceNode className) {
        if (!hasClass(requiredClassName)) {
            throw new AnalyzerException("Class '%s' cannot be found at '%s'"
                    .formatted(requiredClassName.value(), currentPath));
        } else if (!hasClass(className)) {
            throw new AnalyzerException("Class '%s' cannot be found at '%s'"
                    .formatted(className.value(), currentPath));
        }

        return classes.get(requiredClassName).isAssignableFrom(this, classes.get(className));
    }

    @NotNull
    public List<ReferenceNode> argumentTypes(@NotNull ArgumentsNode arguments) {
        return argumentTypes(arguments, true);
    }

    @NotNull
    public List<ReferenceNode> argumentTypes(@NotNull ArgumentsNode arguments, boolean checkInitialized) {
        return arguments.expressions()
                .stream()
                .map(expressionNode -> expressionNode.type(this, false, checkInitialized))
                .collect(Collectors.toList());
    }

    @NotNull
    public AnalyzableClass currentClass(@NotNull String messagePrefix) {
        if (currentClass == null) {
            throw new AnalyzerException("%s at '%s' is invalid: outside of the class context"
                    .formatted(messagePrefix, currentPath()));
        }

        return currentClass;
    }

    @NotNull
    public AnalyzableMethod currentMethod(@NotNull String messagePrefix) {
        if (currentMethod == null) {
            throw new AnalyzerException("%s at '%s' is invalid: outside of the method context"
                    .formatted(messagePrefix, currentPath()));
        }

        return currentMethod;
    }
}
