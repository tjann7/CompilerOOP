package olang;

public class Real extends Any {

    private final float value;

    public Real() {
        this(0);
    }

    public Real(float value) {
        this.value = value;
    }

    public Real add(Real other) {
        return new Real(value + other.value);
    }

    public Real subtract(Real other) {
        return new Real(value - other.value);
    }

    public Real divide(Real other) {
        return new Real(value / other.value);
    }

    public Real multiply(Real other) {
        return new Real(value * other.value);
    }

    public Real mod(Real other) {
        return new Real(value % other.value);
    }

    public Boolean equal(Real other) {
        return new Boolean(value == other.value);
    }

    public Boolean greater(Real other) {
        return new Boolean(value > other.value);
    }

    public Boolean greaterOrEqual(Real other) {
        return new Boolean(value >= other.value);
    }

    public Boolean lower(Real other) {
        return new Boolean(value < other.value);
    }

    public Boolean lowerOrEqual(Real other) {
        return new Boolean(value <= other.value);
    }

    public Integer toInteger() {
        return new Integer((int) value);
    }

    public float java$value() {
        return value;
    }

    @Override
    public Integer hash() {
        return new Integer(Float.hashCode(value));
    }

    @Override
    public Boolean equals(Any other) {
        return new Boolean(
                other.getClass() == getClass()
                        && value == ((Real) other).value
        );
    }
}
