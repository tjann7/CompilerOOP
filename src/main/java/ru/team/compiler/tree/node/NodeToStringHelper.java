package ru.team.compiler.tree.node;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public final class NodeToStringHelper {

    private static final List<Color> AVAILABLE_COLORS = new ArrayList<>(List.of(Color.values()));
    static {
        AVAILABLE_COLORS.remove(Color.WHITE);
    }

    private NodeToStringHelper() {

    }

    @SneakyThrows
    @NotNull
    public static String toString(@NotNull TreeNode treeNode, boolean colored) {
        return toString(treeNode, colored, 0);
    }

    @SneakyThrows
    @NotNull
    public static String toString(@NotNull TreeNode treeNode, boolean colored, int depth) {
        StringBuilder stringBuilder = new StringBuilder();

        String color = colored ? color(depth).code : "";
        stringBuilder.append(color).append(treeNode.getClass().getSimpleName()).append("(");

        AtomicBoolean hasNode = new AtomicBoolean(false);
        boolean hasFields = false;
        Field[] fields = treeNode.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            String name = field.getName();

            Object object = field.get(treeNode);
            String value = toString(object, colored, depth + 1, hasNode);

            stringBuilder.append("\n    ").append(name).append("=").append(value).append(color).append(",");

            hasFields = true;
        }

        if (!hasFields) {
            return stringBuilder.append(")").toString();
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        String string = stringBuilder.append("\n)").toString();

        return hasNode.get() ? string : string.replace("\n    ", "").replace("\n", "");
    }

    @NotNull
    private static String toString(@Nullable Object object, boolean colored, int depth,
                                   @NotNull AtomicBoolean hasNode) {
        if (object instanceof TreeNode fieldNode) {
            hasNode.set(true);
            return toString(fieldNode, colored, depth).replace("\n", "\n    ");
        } else if (object instanceof ExpressionNode.IdArg idArg) {
            hasNode.set(true);

            String name = toString(idArg.name(), colored, depth + 1).replace("\n", "\n    ");
            String arguments = idArg.arguments() != null
                    ? toString(idArg.arguments(), colored, depth + 1).replace("\n", "\n    ")
                    : toString(null, colored, depth + 1, hasNode);

            String color = colored ? color(depth).code : "";

            return (color + "IdArg(\n    name=" + name
                    + color + ",\n    arguments=" + arguments
                    + color + "\n)").replace("\n", "\n    ");
        } else if (object instanceof ParametersNode.Par par) {
            hasNode.set(true);

            String name = toString(par.name(), colored, depth + 1).replace("\n", "\n    ");
            String type = toString(par.type(), colored, depth + 1).replace("\n", "\n    ");

            String color = colored ? color(depth).code : "";

            return (color + "Par(\n    name=" + name
                    + color + ",\n    type=" + type
                    + color + "\n)").replace("\n", "\n    ");
        } else if (object instanceof List<?> list) {
            AtomicBoolean line = new AtomicBoolean(false);

            String color = colored ? color(depth).code : "";

            String value = list.stream()
                    .map(o -> {
                        AtomicBoolean newHasNode = new AtomicBoolean(false);
                        String string = toString(o, colored, depth + 1, newHasNode);

                        if (newHasNode.get()) {
                            hasNode.set(true);
                            line.set(true);
                            return "\n        " + string.replace("\n", "\n    ");
                        }

                        return string;
                    })
                    .collect(Collectors.joining(", ", color + "[", "]"));
            if (line.get()) {
                value = value.substring(0, value.length() - 1) + "\n    ]";
            }
            value = value.substring(0, value.length() - 1) + color + "]";
            return value;
        } else {
            String color = colored ? color(depth).code : "";
            return color + object;
        }
    }

    @NotNull
    private static Color color(int depth) {
        return AVAILABLE_COLORS.get(Math.abs(depth) % AVAILABLE_COLORS.size());
    }

    private enum Color {
        BLACK("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),
        CYAN("\u001B[36m"),
        WHITE("\u001B[37m");

        private final String code;

        Color(@NotNull String code) {
            this.code = code;
        }
    }

}