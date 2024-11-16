package olang;

public class Any {

    public Integer hash() {
        return new Integer(System.identityHashCode(this));
    }

    public Boolean equals(Any other) {
        return new Boolean(this == other);
    }

    @Override
    public final int hashCode() {
        return hash().java$value();
    }

    @Override
    public final boolean equals(Object other) {
        if (other instanceof Any any) {
            return equals(any).java$value();
        }

        return false;
    }
}
