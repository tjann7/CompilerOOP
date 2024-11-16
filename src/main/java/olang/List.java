package olang;

import java.util.ArrayList;

public class List extends Collection {

    private final java.util.List<Any> list = new ArrayList<>();

    public List() {

    }

    public List(Collection collection) {
        list.addAll(collection.java$collection());
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
    public List copy() {
        List l = new List();
        l.list.addAll(list);
        return l;
    }

    @Override
    public java.util.Collection<Any> java$collection() {
        return list;
    }

    public Any get(Integer index) {
        return list.get(index.java$value());
    }
}
