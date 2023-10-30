package net.woggioni.wson.wcfg;

import lombok.RequiredArgsConstructor;
import net.woggioni.jwo.LazyValue;
import net.woggioni.wson.traversal.ValueIdentity;
import net.woggioni.wson.value.ObjectValue;
import net.woggioni.wson.xface.Value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class CompositeObjectValue implements ObjectValue {

    private final List<? extends Value> elements;

    private LazyValue<ObjectValue> wrapped;

    public CompositeObjectValue(List<? extends Value> elements, Value.Configuration cfg) {
        this.elements = elements;
        this.wrapped = LazyValue.of(() -> {
            ObjectValue result = ObjectValue.newInstance(cfg);
            List<ValueIdentity> identities = new ArrayList<>();
            for (Value element : elements) {
                if (element instanceof CompositeObjectValue compositeObjectValue) {
                    boolean differenceFound = false;
                    for (int i = 0; i < compositeObjectValue.elements.size(); i++) {
                        ObjectValue objectValue = (ObjectValue) compositeObjectValue.elements.get(i);
                        if (!differenceFound && (i >= identities.size() || !Objects.equals(
                                identities.get(i),
                                new ValueIdentity(compositeObjectValue.elements.get(i))))) {
                            differenceFound = true;
                        }
                        if (differenceFound) {
                            merge(result, objectValue);
                            identities.add(new ValueIdentity(objectValue));
                        }
                    }
                } else {
                    merge(result, (ObjectValue) element);
                    identities.add(new ValueIdentity(element));
                }
            }
            return result;
        }, LazyValue.ThreadSafetyMode.NONE);
    }

    private static void merge(ObjectValue v1, ObjectValue v2) {
        for (Map.Entry<String, Value> entry : v2) {
            String key = entry.getKey();
            Value value2put = entry.getValue();
            Value putResult = v1.getOrPut(key, value2put);
            if (putResult != value2put) {
                if (putResult.type() == Value.Type.OBJECT && value2put.type() == Value.Type.OBJECT) {
                    ObjectValue ov = ObjectValue.newInstance();
                    merge(ov, (ObjectValue) putResult);
                    merge(ov, (ObjectValue) value2put);
                    v1.put(key, ov);
                } else {
                    v1.put(key, value2put);
                }
            }
        }
    }

    @Override
    public Iterator<Map.Entry<String, Value>> iterator() {
        return wrapped.get().iterator();
    }

    @Override
    public Value get(String key) {
        return wrapped.get().get(key);
    }

    @Override
    public Value getOrDefault(String key, Value defaultValue) {
        return wrapped.get().getOrDefault(key, defaultValue);
    }

    @Override
    public boolean has(String key) {
        return wrapped.get().has(key);
    }

    @Override
    public int size() {
        return wrapped.get().size();
    }

    @Override
    public Iterable<Map.Entry<String, Value>> asObject() {
        return this::iterator;
    }
}
