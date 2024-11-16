package olang;

import java.util.HashSet;

public class Set extends Collection {

    private final java.util.Set<Any> set = new HashSet<>();

    public Set() {

    }

    public Set(Collection collection) {
        set.addAll(collection.java$collection());
    }

    @Override
    public Boolean add(Any element) {
        return new Boolean(set.add(element));
    }

    @Override
    public Boolean remove(Any element) {
        return new Boolean(set.remove(element));
    }

    @Override
    public Integer size() {
        return new Integer(set.size());
    }

    @Override
    public Set copy() {
        Set s = new Set();
        s.set.addAll(set);
        return s;
    }

    @Override
    public java.util.Collection<Any> java$collection() {
        return set;
    }
}
