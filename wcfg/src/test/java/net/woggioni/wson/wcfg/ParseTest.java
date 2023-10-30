package net.woggioni.wson.wcfg;

import lombok.SneakyThrows;
import net.woggioni.wson.serialization.json.JSONDumper;
import net.woggioni.wson.value.ObjectValue;
import net.woggioni.wson.xface.Value;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

public class ParseTest {

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {
        "build.wcfg",
        "test.wcfg",
        "recursive.wcfg",
        "recursive2.wcfg",
        "recursive3.wcfg",
    })
    public void test(String resource) {
        Value.Configuration cfg = Value.Configuration.builder()
                .objectValueImplementation(ObjectValue.Implementation.HashMap)
                .serializeReferences(true)
                .build();
        try(Reader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource))) {
            WConfig wcfg = WConfig.parse(reader, cfg);
            new JSONDumper(cfg).dump(wcfg.whole(), System.out);
            System.out.println();
        }
    }

    @Test
    @SneakyThrows
    public void test2() {
        Value.Configuration cfg = Value.Configuration.builder().serializeReferences(true).build();
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/build.wcfg"))) {
            WConfig wcfg = WConfig.parse(reader, cfg);
            Value result = wcfg.get("release", "dev");
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream("/tmp/build.json"))) {
                new JSONDumper(cfg).dump(result, os);
            }
        }
    }
}
