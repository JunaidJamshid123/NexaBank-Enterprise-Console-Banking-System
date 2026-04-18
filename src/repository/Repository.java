package repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Repository<T, ID> {
    void save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    List<T> findAll(Predicate<T> filter);
    void delete(ID id);
    boolean exists(ID id);
    long count();
}
