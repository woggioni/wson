package net.woggioni.wson.wcfg;

import lombok.SneakyThrows;
import net.woggioni.wson.value.ObjectValue;
import net.woggioni.wson.xface.Value;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.Reader;
import java.util.Arrays;
import java.util.stream.Collectors;

public class WConfig {

    private final Value.Configuration cfg;
    private final Value value;

    @SneakyThrows
    private WConfig(Reader reader, Value.Configuration cfg) {
        this.cfg = cfg;
        CodePointCharStream inputStream = CharStreams.fromReader(reader);
        WCFGLexer lexer = new WCFGLexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        WCFGParser parser = new WCFGParser(commonTokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(new ErrorHandler());
        ListenerImpl listener = new ListenerImpl(cfg);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, parser.wcfg());
        value = listener.getResult();
    }

    public Value get(String key) {
        return value.get(key);
    }

    public Value get(String ...overrides) {
        return new CompositeObjectValue(
                Arrays.stream(overrides)
                    .map(k -> (ObjectValue) value.get(k))
                    .collect(Collectors.toList()), cfg);
    }

    public Value whole() {
        return value;
    }

    public static WConfig parse(Reader reader, Value.Configuration cfg) {
        return new WConfig(reader, cfg);
    }
}
