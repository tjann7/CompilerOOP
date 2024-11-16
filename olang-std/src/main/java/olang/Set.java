package olang;

import java.util.HashSet;
import java.util.Objects;

public class Set extends Collection {

    private final java.util.Set<Any> set = new HashSet<>();

    public Set() {

    }

    public Set(Collection collection) {
        collection.iterator().java$iterator().forEachRemaining(set::add);
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
    public Integer hash() {
        return new Integer(set.hashCode());
    }

    @Override
    public Boolean equals(Any other) {
        return new Boolean(
                other.getClass() == getClass()
                        && Objects.equals(set, ((Set) other).set)
        );
    }
}
