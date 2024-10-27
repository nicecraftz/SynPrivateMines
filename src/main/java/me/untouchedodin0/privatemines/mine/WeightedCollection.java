package me.untouchedodin0.privatemines.mine;


import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public class WeightedCollection<E> {
    private final NavigableMap<Double, E> map = new TreeMap<>();
    private double total = 0;

    public void add(double weight, E result) {
        if (weight <= 0) return;
        total += weight;
        map.put(total, result);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public E next() {
        double value = Math.random() * total;
        return map.higherEntry(value).getValue();
    }

    public void clear() {
        map.clear();
    }

    public Set<Map.Entry<Double, E>> entrySet() {
        return Set.copyOf(map.entrySet());
    }

    public static <T> WeightedCollection<T> single(T value, double chance) {
        WeightedCollection<T> collection = new WeightedCollection<>();
        collection.add(chance, value);
        return collection;
    }

}