package net.woggioni.wson.value;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.woggioni.wson.xface.Value;

@EqualsAndHashCode
public class StringValue implements Value {

    private final String value;

    public StringValue(@NonNull String value) {
        this.value = value;
    }

    @Override
    public Type type() {
        return Type.STRING;
    }

    @Override
    public String asString() {
        return value;
    }
}
