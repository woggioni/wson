package net.woggioni.wson.cli;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.woggioni.jwo.JWO.newThrowable;

@RequiredArgsConstructor
enum SerializationFormat {
    JSON("json"), JBON("jbon");

    private final String name;


    public static SerializationFormat parse(String value) {
        return Arrays.stream(SerializationFormat.values())
                .filter(sf -> Objects.equals(sf.name, value))
                .findFirst()
                .orElseThrow(() -> {
                    String availableValues = Stream.of(
                            JSON,
                            JBON
                    ).map(SerializationFormat::name).collect(Collectors.joining(", "));
                    throw newThrowable(IllegalArgumentException.class,
                            "Unknown serialization format '%s', possible values are %s",
                            value, availableValues
                    );
                });
    }
}
