package olang;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class List extends Collection implements Native$ToString {

    private final java.util.List<Any> list = new ArrayList<>();

    public List() {

    }

    public List(Collection collection) {
        collection.iterator().java$iterator().forEachRemaining(list::add);
    }

    @Override
    public Boolean add(Any element) {
        return new Boolean(list.add(element));
    }

    @Override
    public Boolean remove(Any element) {
        return new Boolean(list.remove(element));
    }

    @Override
    public Integer size() {
        return new Integer(list.size());
    }

    @Override
    public Iterator iterator() {
        return Iterator.java$wrap(list.iterator());
    }

    @Override
    public List copy() {
        List l = new List();
        l.list.addAll(list);
        return l;
    }

    public Any get(Integer index) {
        return list.get(index.java$value());
    }

    @Override
    public Integer hash() {
        return new Integer(list.hashCode());
    }

    @Override
    public Boolean equals(Any other) {
        return new Boolean(
                other.getClass() == getClass()
                        && Objects.equals(list, ((List) other).list)
        );
    }

    @Override
    public String native$toString(Console console) {
        return "List" + list.stream()
                .map(console::native$toString)
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
