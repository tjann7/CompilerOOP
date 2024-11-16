package olang;

public class Integer extends Any {

    private final int value;

    public Integer() {
        this(0);
    }

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

    public Boolean equal(Integer other) {
        return new Boolean(value == other.value);
    }

    public Boolean greater(Integer other) {
        return new Boolean(value > other.value);
    }

    public Boolean greaterOrEqual(Integer other) {
        return new Boolean(value >= other.value);
    }

    public Boolean lower(Integer other) {
        return new Boolean(value < other.value);
    }

    public Boolean lowerOrEqual(Integer other) {
        return new Boolean(value <= other.value);
    }

    public Real toReal() {
        return new Real((float) value);
    }

    public int java$value() {
        return value;
    }

    @Override
    public Integer hash() {
        return this;
    }

    @Override
    public Boolean equals(Any other) {
        return new Boolean(
                other.getClass() == getClass()
                        && value == ((Integer) other).value
        );
    }
}
