package net.woggioni.wson.jakarta;

import jakarta.json.JsonValue;
import lombok.RequiredArgsConstructor;
import net.woggioni.wson.xface.Value;

@RequiredArgsConstructor
public class WsonJsonValue implements JsonValue {
    private final Value value;

    @Override
    public ValueType getValueType() {
        ValueType result;
        switch (value.type()) {
            case NULL:
                result = ValueType.NULL;
                break;
            case OBJECT:
                result = ValueType.OBJECT;
                break;
            case ARRAY:
                result = ValueType.ARRAY;
                break;
            case STRING:
                result = ValueType.STRING;
                break;
            case BOOLEAN:
                result = value.asBoolean() ? ValueType.TRUE : ValueType.FALSE;
                break;
            case DOUBLE:
            case INTEGER:
                result = ValueType.NUMBER;
                break;
            default:
                throw new UnsupportedOperationException("This should never happen");
        }
        return result;
    }
//
//    @Override
//    public JsonObject getJsonObject(int index) {
//        return new WsonJsonValue(value.get(index));
//    }
//
//    @Override
//    public JsonArray getJsonArray(int index) {
//        return new WsonJsonValue(value.get(index));
//    }
//
//    @Override
//    public JsonNumber getJsonNumber(int index) {
//        return new WsonJsonValue(value.get(index));
//    }
//
//    @Override
//    public JsonString getJsonString(int index) {
//        return new WsonJsonValue(value.get(index));
//    }
//
//    @Override
//    public <T extends JsonValue> List<T> getValuesAs(Class<T> clazz) {
//        return (List<T>) JWO.iterable2Stream(value.asArray()).map(WsonJsonValue::new)
//            .collect(CollectionUtils.toUnmodifiableList());
//    }
//
//    @Override
//    public String getString(int index) {
//        return value.get(index).asString();
//    }
//
//    @Override
//    public String getString(int index, String defaultValue) {
//        if(index < value.size()) return value.get(index).asString();
//        else return defaultValue;
//    }
//
//    @Override
//    public int getInt(int index) {
//        return (int) value.get(index).asInteger();
//    }
//
//    @Override
//    public int getInt(int index, int defaultValue) {
//        if(index < value.size()) return (int) value.get(index).asInteger();
//        else return defaultValue;
//    }
//
//    @Override
//    public boolean getBoolean(int index) {
//        return value.get(index).asBoolean();
//    }
//
//    @Override
//    public boolean getBoolean(int index, boolean defaultValue) {
//        if(index < value.size()) return value.get(index).asBoolean();
//        else return defaultValue;
//    }
//
//    @Override
//    public boolean isNull(int index) {
//        return false;
//    }
//
//    @Override
//    public JsonArray getJsonArray(String name) {
//        return new WsonJsonValue(value.get(name));
//    }
//
//    @Override
//    public JsonObject getJsonObject(String name) {
//        return new WsonJsonValue(value.get(name));
//    }
//
//    @Override
//    public JsonNumber getJsonNumber(String name) {
//        return new WsonJsonValue(value.get(name));
//    }
//
//    @Override
//    public JsonString getJsonString(String name) {
//        return new WsonJsonValue(value.get(name));
//    }
//
//    @Override
//    public String getString(String name) {
//        return value.get(name).asString();
//    }
//
//    @Override
//    public String getString(String name, String defaultValue) {
//        Value result = value.getOrDefault(name, Value.Null);
//        if(result.isNull()) return defaultValue;
//        else return result.asString();
//    }
//
//    @Override
//    public int getInt(String name) {
//        return (int) value.get(name).asInteger();
//    }
//
//    @Override
//    public int getInt(String name, int defaultValue) {
//        Value result = value.getOrDefault(name, Value.Null);
//        if(result.isNull()) return defaultValue;
//        else return (int) result.asInteger();
//    }
//
//    @Override
//    public boolean getBoolean(String name) {
//        return value.get(name).asBoolean();
//    }
//
//    @Override
//    public boolean getBoolean(String name, boolean defaultValue) {
//        Value result = value.getOrDefault(name, Value.Null);
//        if(result.isNull()) return defaultValue;
//        else return result.asBoolean();
//    }
//
//    @Override
//    public boolean isNull(String name) {
//        return value.isNull();
//    }
//
//    @Override
//    public int size() {
//        return value.size();
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return value.size() == 0;
//    }
//
//    @Override
//    public boolean contains(Object o) {
//        boolean result = false;
//        switch (value.type()) {
//            case ARRAY:
//                for(Value v : value.asArray()) {
//                    if(v == o) {
//                        result = true;
//                        break;
//                    }
//                }
//                break;
//            case OBJECT:
//                for(Map.Entry<String, Value> entry : value.asObject()) {
//                    if(entry.getValue() == o) {
//                        result = true;
//                        break;
//                    }
//                }
//                break;
//            default:
//                throw newThrowable(ClassCastException.class, "This value is not of type %s", ValueType.ARRAY);
//        }
//        return result;
//    }
//
//    @Override
//    public Iterator<JsonValue> iterator() {
//        return new Iterator<>() {
//            private final Iterator<Value> it = value.asArray().iterator();
//            @Override
//            public boolean hasNext() {
//                return it.hasNext();
//            }
//
//            @Override
//            public JsonValue next() {
//                return new WsonJsonValue(it.next());
//            }
//        };
//    }
//
//    @Override
//    public Object[] toArray() {
//        return new Object[0];
//    }
//
//    @Override
//    public <T> T[] toArray(T[] a) {
//        return null;
//    }
//
//    @Override
//    public boolean add(JsonValue jsonValue) {
//        return false;
//    }
//
//    @Override
//    public boolean remove(Object o) {
//        return false;
//    }
//
//    @Override
//    public boolean containsAll(Collection<?> c) {
//        return false;
//    }
//
//    @Override
//    public boolean addAll(Collection<? extends JsonValue> c) {
//        return false;
//    }
//
//    @Override
//    public boolean addAll(int index, Collection<? extends JsonValue> c) {
//        return false;
//    }
//
//    @Override
//    public boolean removeAll(Collection<?> c) {
//        return false;
//    }
//
//    @Override
//    public boolean retainAll(Collection<?> c) {
//        return false;
//    }
//
//    @Override
//    public void clear() {
//
//    }
//
//    @Override
//    public JsonValue get(int index) {
//        return null;
//    }
//
//    @Override
//    public JsonValue set(int index, JsonValue element) {
//        return null;
//    }
//
//    @Override
//    public void add(int index, JsonValue element) {
//
//    }
//
//    @Override
//    public JsonValue remove(int index) {
//        return null;
//    }
//
//    @Override
//    public int indexOf(Object o) {
//        return 0;
//    }
//
//    @Override
//    public int lastIndexOf(Object o) {
//        return 0;
//    }
//
//    @Override
//    public ListIterator<JsonValue> listIterator() {
//        return null;
//    }
//
//    @Override
//    public ListIterator<JsonValue> listIterator(int index) {
//        return null;
//    }
//
//    @Override
//    public List<JsonValue> subList(int fromIndex, int toIndex) {
//        return null;
//    }
//
//    @Override
//    public boolean containsKey(Object key) {
//        return false;
//    }
//
//    @Override
//    public boolean containsValue(Object value) {
//        return false;
//    }
//
//    @Override
//    public JsonValue get(Object key) {
//        return null;
//    }
//
//    @Override
//    public JsonValue put(String key, JsonValue value) {
//        return null;
//    }
//
//    @Override
//    public void putAll(Map<? extends String, ? extends JsonValue> m) {
//
//    }
//
//    @Override
//    public Set<String> keySet() {
//        return null;
//    }
//
//    @Override
//    public Collection<JsonValue> values() {
//        return null;
//    }
//
//    @Override
//    public Set<Entry<String, JsonValue>> entrySet() {
//        return null;
//    }
//
//    @Override
//    public JsonObject asJsonObject() {
//        return this;
//    }
//
//    @Override
//    public JsonArray asJsonArray() {
//        return this;
//    }
}
