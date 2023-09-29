package net.woggioni.wson.value;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.woggioni.wson.xface.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@EqualsAndHashCode
public class ArrayValue implements Value, Iterable<Value> {

    private final List<Value> value;

    public ArrayValue() {
        this.value = new ArrayList<>();
    }

    public ArrayValue(@NonNull List<Value> value) {
        this.value = value;
    }

    @Override
    public Type type() {
        return Type.ARRAY;
    }

    @Override
    public void add(Value value) {
        this.value.add(value);
    }

    @Override
    public void set(int index, Value value) {
        this.value.set(index, value);
    }

    @Override
    public Value get(int index) {
        int sz = size();
        if(index < sz) {
            return value.get(Math.floorMod(index, sz));
        } else {
            return Value.Null;
        }
    }

    @Override
    public Value pop() {
        Value last = tail();
        value.remove(value.size() - 1);
        return last;
    }

    @Override
    public Value head() {
        return value.get(0);
    }

    @Override
    public Value tail() {
        return value.get(value.size() - 1);
    }

    @Override
    public List<Value> asArray() {
        return Collections.unmodifiableList(value);
    }

    @Override
    public Iterator<Value> iterator() {
        return value.iterator();
    }

    @Override
    public int size() {
        return value.size();
    }
}
