package olang;

import java.lang.reflect.Field;

public class Console extends Any {

    public void print(Any any) {
        System.out.print(toString(any));
    }

    public void println(Any any) {
        System.out.println(toString(any));
    }

    private String toString(Any any) {
        try {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(getClass().getSimpleName()).append("{");

            Field[] fields = getClass().getFields();
            for (Field field : fields) {
                stringBuilder.append(field.getName()).append("=");
                stringBuilder.append(field.get(this).toString());
                stringBuilder.append(", ");
            }

            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());

            stringBuilder.append("}");

            return stringBuilder.toString();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot handle toString", e);
        }
    }
}
