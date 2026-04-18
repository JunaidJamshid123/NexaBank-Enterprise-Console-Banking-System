package repository.impl;

import model.user.Customer;
import repository.Repository;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * In-memory customer repository backed by HashMap.
 *
 * HashMap<String, Customer> — O(1) lookup by customer ID.
 */
public class InMemoryCustomerRepository implements Repository<Customer, String> {

    private final HashMap<String, Customer> store = new HashMap<>();

    @Override
    public void save(Customer entity) {
        store.put(String.valueOf(entity.getId()), entity);
    }

    @Override
    public Optional<Customer> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Customer> findAll(Predicate<Customer> filter) {
        return store.values().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

    @Override
    public boolean exists(String id) {
        return store.containsKey(id);
    }

    @Override
    public long count() {
        return store.size();
    }

    public Optional<Customer> findByEmail(String email) {
        return store.values().stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
