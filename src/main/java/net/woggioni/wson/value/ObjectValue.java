package net.woggioni.wson.value;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.woggioni.wson.xface.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static net.woggioni.jwo.JWO.newThrowable;

public interface ObjectValue extends Value, Iterable<Map.Entry<String, Value>> {

    static ObjectValue newInstance() {
        return newInstance(Value.configuration);
    }

    static ObjectValue newInstance(Configuration cfg) {
        ObjectValue result;
        switch(cfg.objectValueImplementation) {
            case ArrayList:
                result = new ListObjectValue();
                break;
            case TreeMap:
                result = new TreeMapObjectValue();
                break;
            case HashMap:
                result = new HashMapObjectValue();
                break;
            case LinkedHashMap:
                result = new LinkedHashMapObjectValue();
                break;
            default:
                throw newThrowable(IllegalArgumentException.class,
                    "Unknown value of %s: %s",
                    Implementation.class.getName(),
                    cfg.objectValueImplementation);
        }
        return result;
    }

    @Override
    default Type type() {
        return Type.OBJECT;
    }

    enum Implementation {
        ArrayList, TreeMap, HashMap, LinkedHashMap
    }
}

@EqualsAndHashCode
final class ObjectEntry<K, V> implements Map.Entry<K, V> {
    private final K key;
    private V value;

    public ObjectEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }
}

@EqualsAndHashCode
abstract class MapObjectValue implements ObjectValue {

    private final Map<String, Value> value;

    public MapObjectValue(Map<String, Value> value) {
        this.value = value;
    }

    @Override
    public Iterable<Map.Entry<String, Value>> asObject() {
        return () -> value.entrySet().iterator();
    }

    @Override
    public Value get(String key) {
        return value.get(key);
    }

    @Override
    public Value getOrDefault(String key, Value defaultValue) {
        return value.getOrDefault(key, defaultValue);
    }

    @Override
    public Value getOrPut(String key, Value value2Put) {
        if (value.containsKey(key))
            return value.get(key);
        else {
            put(key, value2Put);
            return value2Put;
        }
    }

    @Override
    public void put(String key, Value value2Put) {
        this.value.put(key, value2Put);
    }


    @Override
    public boolean has(String key) {
        return value.containsKey(key);
    }

    @Override
    public Iterator<Map.Entry<String, Value>> iterator() {
        return value.entrySet().iterator();
    }

    @Override
    public int size() {
        return value.size();
    }
}

@EqualsAndHashCode(callSuper = true)
class HashMapObjectValue extends MapObjectValue {

    public HashMapObjectValue() {
        super(new HashMap<>());
    }
}

@EqualsAndHashCode(callSuper = true)
class LinkedHashMapObjectValue extends MapObjectValue {

    public LinkedHashMapObjectValue() {
        super(new LinkedHashMap<>());
    }
}

@EqualsAndHashCode(callSuper = true)
class TreeMapObjectValue extends MapObjectValue {

    public TreeMapObjectValue() {
        super(new TreeMap<>());
    }
}


@NoArgsConstructor
@EqualsAndHashCode
class ListObjectValue implements ObjectValue {

    @EqualsAndHashCode.Include
    private final List<Map.Entry<String, Value>> value = new ArrayList<>();

    public ListObjectValue(Map<String, Value> map) {
        this.value.addAll(map.entrySet());
    }

    @Override
    public Iterable<Map.Entry<String, Value>> asObject() {
        return value::iterator;
    }

    @Override
    public Value get(String key) {
        for (Map.Entry<String, Value> entry : value) {
            if(Objects.equals(entry.getKey(), key)) return entry.getValue();
        }
        return null;
    }

    @Override
    public Value getOrDefault(String key, Value defaultValue) {
        for (Map.Entry<String, Value> entry : value) {
            if(Objects.equals(entry.getKey(), key)) return entry.getValue();
        }
        return defaultValue;
    }

    @Override
    public Value getOrPut(String key, Value value2Put) {
        for (Map.Entry<String, Value> entry : value) {
            if(Objects.equals(entry.getKey(), key)) return entry.getValue();
        }
        put(key, value2Put);
        return value2Put;
    }

    @Override
    public void put(String key, Value value2Put) {
        value.add(new ObjectEntry<>(key, value2Put));
    }


    @Override
    public boolean has(String key) {
        for (Map.Entry<String, Value> entry : value) {
            if(Objects.equals(entry.getKey(), key)) return true;
        }
        return false;
    }

    @Override
    public Iterator<Map.Entry<String, Value>> iterator() {
        return value.iterator();
    }

    @Override
    public int size() {
        return value.size();
    }
}
