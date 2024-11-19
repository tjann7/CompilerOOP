package olang;

import java.lang.reflect.Field;

public class Console extends Any {

    public void print(Any any) {
        System.out.print(native$toString(any));
    }

    public void println(Any any) {
        System.out.println(native$toString(any));
    }

    public String native$toString(Any any) {
        if (any instanceof Native$ToString native$ToString) {
            return native$ToString.native$toString(this);
        }

        try {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(any.getClass().getSimpleName()).append("{");

            Field[] fields = any.getClass().getFields();
            for (Field field : fields) {
                stringBuilder.append(field.getName()).append("=");

                Object o = field.get(any);
                if (o instanceof Any a) {
                    stringBuilder.append(native$toString(a));
                } else {
                    stringBuilder.append(o.toString());
                }

                stringBuilder.append(", ");
            }

            if (fields.length > 0) {
                stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
            }

            stringBuilder.append("}");

            return stringBuilder.toString();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot handle toString", e);
        }
    }
}
