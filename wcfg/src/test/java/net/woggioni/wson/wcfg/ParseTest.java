package net.woggioni.wson.wcfg;

import lombok.SneakyThrows;
import net.woggioni.wson.serialization.json.JSONDumper;
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
//        "build.wcfg",
//        "test.wcfg",
//        "recursive.wcfg",
        "recursive2.wcfg",
    })
    public void test(String resource) {
        try(Reader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource))) {
            CodePointCharStream inputStream = CharStreams.fromReader(reader);
            WCFGLexer lexer = new WCFGLexer(inputStream);
            CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
            WCFGParser parser = new WCFGParser(commonTokenStream);
            Value.Configuration cfg = Value.Configuration.builder().serializeReferences(true).build();
            ListenerImpl listener = new ListenerImpl(cfg);
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, parser.wcfg());
            listener.replaceHolders();
            Value result = listener.getResult();
            new JSONDumper(cfg).dump(result, System.out);
        }
    }

    @Test
    @SneakyThrows
    public void test2() {
        Value.Configuration cfg = Value.Configuration.builder().serializeReferences(true).build();
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/build.wcfg"))) {
            WConfig wConfig = new WConfig(reader, cfg);
            Value result = wConfig.get("release", "dev");
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream("/tmp/build.json"))) {
                new JSONDumper(cfg).dump(result, os);
            }
        }
    }
}
