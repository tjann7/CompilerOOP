package olang;

public class Boolean extends Any implements Native$ToString {

    private final boolean value;

    public Boolean() {
        this(false);
    }

    public Boolean(boolean value) {
        this.value = value;
    }

    public Boolean not() {
        return new Boolean(!value);
    }

    public Boolean and(Boolean other) {
        return new Boolean(value && other.value);
    }

    public Boolean or(Boolean other) {
        return new Boolean(value || other.value);
    }

    public Boolean xor(Boolean other) {
        return new Boolean(value ^ other.value);
    }

    public boolean java$value() {
        return value;
    }

    @Override
    public Integer hash() {
        return new Integer(value ? 1 : 0);
    }

    @Override
    public Boolean equals(Any other) {
        return new Boolean(
                other.getClass() == getClass()
                        && value == ((Boolean) other).value
        );
    }

    @Override
    public String native$toString(Console console) {
        return String.valueOf(value);
    }
}
