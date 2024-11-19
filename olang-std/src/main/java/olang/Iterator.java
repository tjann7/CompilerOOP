package olang;

public class Iterator extends Any {

    public Iterator() {

    }

    public static Iterator java$wrap(java.util.Iterator<Any> iterator) {
        return new Iterator() {
            @Override
            public Boolean hasNext() {
                return new Boolean(iterator.hasNext());
            }

            @Override
            public Any next() {
                return iterator.next();
            }
        };
    }

    public Boolean hasNext() {
        throw new UnsupportedOperationException();
    }

    public Any next() {
        throw new UnsupportedOperationException();
    }

    public java.util.Iterator<Any> java$iterator() {
        return new java.util.Iterator<>() {
            @Override
            public boolean hasNext() {
                return Iterator.this.hasNext().java$value();
            }

            @Override
            public Any next() {
                return Iterator.this.next();
            }
        };
    }
}
