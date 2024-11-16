package olang;

public class Integer extends Any {

    private final int value;

    public Integer(int value) {
        this.value = value;
    }

    public Integer add(Integer other) {
        return new Integer(value + other.value);
    }

    public Integer subtract(Integer other) {
        return new Integer(value - other.value);
    }

    public Integer divide(Integer other) {
        return new Integer(value / other.value);
    }

    public Integer multiply(Integer other) {
        return new Integer(value * other.value);
    }

    public Integer mod(Integer other) {
        return new Integer(value % other.value);
    }

    public Real toReal() {
        return new Real((float) value);
    }

    public int java$value() {
        return value;
    }
}
