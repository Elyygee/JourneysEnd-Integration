package abeshutt.staracademy.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class FilteringIterable<INPUT> implements Collection<INPUT> {

    private final Iterable<INPUT> parent;
    private final Predicate<INPUT> filter;

    public FilteringIterable(Iterable<INPUT> parent, Predicate<INPUT> filter) {
        this.parent = parent;
        this.filter = filter;
    }

    @Override
    public Iterator<INPUT> iterator() {
        return new FilteringIterator<>(this.parent.iterator(), this.filter);
    }

    // Collection interface methods - not supported for filtering iterables
    @Override
    public int size() {
        throw new UnsupportedOperationException("FilteringIterable does not support size()");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("FilteringIterable does not support isEmpty()");
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("FilteringIterable does not support contains()");
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("FilteringIterable does not support toArray()");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("FilteringIterable does not support toArray()");
    }

    @Override
    public boolean add(INPUT input) {
        throw new UnsupportedOperationException("FilteringIterable does not support add()");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("FilteringIterable does not support remove()");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("FilteringIterable does not support containsAll()");
    }

    @Override
    public boolean addAll(Collection<? extends INPUT> c) {
        throw new UnsupportedOperationException("FilteringIterable does not support addAll()");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("FilteringIterable does not support removeAll()");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("FilteringIterable does not support retainAll()");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("FilteringIterable does not support clear()");
    }

}
