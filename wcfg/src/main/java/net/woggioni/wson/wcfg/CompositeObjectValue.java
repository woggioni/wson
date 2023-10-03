package net.woggioni.wson.wcfg;

import lombok.RequiredArgsConstructor;
import net.woggioni.wson.traversal.ValueIdentity;
import net.woggioni.wson.value.ObjectValue;
import net.woggioni.wson.xface.Value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.woggioni.jwo.JWO.dynamicCast;

@RequiredArgsConstructor
public class CompositeObjectValue implements ObjectValue {

    private final List<ObjectValue> elements;

    private ObjectValue wrapped;

    public CompositeObjectValue(List<ObjectValue> elements, Value.Configuration cfg) {
        this.elements = elements;
        wrapped = ObjectValue.newInstance(cfg);
        List<ValueIdentity> identities = new ArrayList<>();
        for (ObjectValue element : elements) {
            CompositeObjectValue compositeObjectValue;
            if ((compositeObjectValue = dynamicCast(element, CompositeObjectValue.class)) != null) {
                boolean differenceFound = false;
                for (int i = 0; i < compositeObjectValue.elements.size(); i++) {
                    ObjectValue objectValue = compositeObjectValue.elements.get(i);
                    if (!differenceFound && (i >= identities.size() || !Objects.equals(
                            identities.get(i),
                            new ValueIdentity(compositeObjectValue.elements.get(i))))) {
                        differenceFound = true;
                    }
                    if (differenceFound) {
                        merge(wrapped, objectValue);
                        identities.add(new ValueIdentity(objectValue));
                    }
                }
            } else {
                merge(wrapped, element);
                identities.add(new ValueIdentity(element));
            }
        }
    }

    private static void merge(ObjectValue v1, ObjectValue v2) {
        for (Map.Entry<String, Value> entry : v2) {
            Value putResult = v1.getOrPut(entry.getKey(), entry.getValue());
            if (putResult != entry.getValue()) {
                if (putResult.type() == Value.Type.OBJECT && entry.getValue().type() == Value.Type.OBJECT) {
                    ObjectValue ov = ObjectValue.newInstance();
                    merge(ov, (ObjectValue) putResult);
                    merge(ov, (ObjectValue) entry.getValue());
                    v1.put(entry.getKey(), ov);
                } else {
                    v1.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public Iterator<Map.Entry<String, Value>> iterator() {
        return wrapped.iterator();
    }

    @Override
    public Value get(String key) {
        return wrapped.get(key);
    }

    @Override
    public Value getOrDefault(String key, Value defaultValue) {
        return wrapped.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean has(String key) {
        return wrapped.has(key);
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public Iterable<Map.Entry<String, Value>> asObject() {
        return this::iterator;
    }
}
