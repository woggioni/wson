package net.woggioni.wson.wcfg;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.woggioni.wson.xface.Value;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor
class ValueHolder implements Value {

    @Getter
    private final TerminalNode node;
    private List<Runnable> deleters = new ArrayList<>();
    public void addDeleter(Runnable runnable) {
        deleters.add(runnable);
    }

    @Setter
    @Getter
    private Value delegate = null;

    @Override
    public Type type() {
        return delegate.type();
    }

    @Override
    public boolean isNull() {
        return delegate.isNull();
    }

    @Override
    public boolean asBoolean() {
        return delegate.asBoolean();
    }

    @Override
    public long asInteger() {
        return delegate.asInteger();
    }

    @Override
    public double asFloat() {
        return delegate.asFloat();
    }

    @Override
    public String asString() {
        return delegate.asString();
    }

    @Override
    public Iterable<Value> asArray() {
        return delegate.asArray();
    }

    @Override
    public Iterable<Map.Entry<String, Value>> asObject() {
        return delegate.asObject();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public void add(Value value) {
        delegate.add(value);
    }

    @Override
    public void set(int index, Value value) {
        delegate.set(index, value);
    }

    @Override
    public Value pop() {
        return delegate.pop();
    }

    @Override
    public Value head() {
        return delegate.head();
    }

    @Override
    public Value tail() {
        return delegate.tail();
    }

    @Override
    public Value get(int index) {
        return delegate.get(index);
    }

    @Override
    public void put(String key, Value value) {
        delegate.put(key, value);
    }

    @Override
    public Value get(String key) {
        return delegate.get(key);
    }

    @Override
    public Value getOrDefault(String key, Value defaultValue) {
        return delegate.getOrDefault(key, defaultValue);
    }

    @Override
    public Value getOrPut(String key, Value value2Put) {
        return delegate.getOrPut(key, value2Put);
    }

    @Override
    public boolean has(String key) {
        return delegate.has(key);
    }

    public void replace() {
        for(Runnable run : deleters) {
            run.run();
        }
    }
}
