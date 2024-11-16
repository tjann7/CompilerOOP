package olang;

public class Real extends Any {

    private final float value;

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

    public Integer toInteger() {
        return new Integer((int) value);
    }

    public float java$value() {
        return value;
    }
}
