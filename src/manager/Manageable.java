package manager;

public interface Manageable<T> {
    boolean add(T item);
    boolean remove(String id);
    boolean update(T item);
    T findById(String id);
}

