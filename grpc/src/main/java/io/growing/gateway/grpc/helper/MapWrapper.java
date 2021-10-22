package io.growing.gateway.grpc.helper;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class MapWrapper<K, V> implements Map<K, V> {

    private Map<K, V> underlying;

    public MapWrapper(Map<K, V> underlying) {
        this.underlying = underlying;
    }

    protected Map<K,V> getUnderlying() {
        return this.underlying;
    }


    @Override
    public int size() {
        return underlying.size();
    }

    @Override
    public boolean isEmpty() {
        return underlying.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return underlying.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return underlying.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return underlying.get(key);
    }

    @Override
    public V put(K key, V value) {
        return underlying.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return underlying.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        underlying.putAll(m);
    }

    @Override
    public void clear() {
        underlying.clear();
    }

    @Override
    public Set<K> keySet() {
        return underlying.keySet();
    }

    @Override
    public Collection<V> values() {
        return underlying.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return underlying.entrySet();
    }

}
