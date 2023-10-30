package net.woggioni.wson.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.woggioni.wson.antlr.JSONLexer;
import net.woggioni.wson.antlr.JSONListenerImpl;
import net.woggioni.wson.serialization.binary.JBONParser;
import net.woggioni.wson.serialization.json.JSONParser;
import net.woggioni.wson.value.ObjectValue;
import net.woggioni.wson.xface.Value;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.tukaani.xz.XZInputStream;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class WsonParserBenchmark {

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

        @SneakyThrows
        public InputStream hugeTestData() {
            return new XZInputStream(Main.class.getResourceAsStream("/citylots.json.xz"));
        }

        @SneakyThrows
        public InputStream hugeBinaryTestData() {
            return new XZInputStream(new BufferedInputStream(WsonParserBenchmark.class.getResourceAsStream("/citylots.jbon.xz")));
        }

        public InputStream smallTestData() {
            return new BufferedInputStream(WsonParserBenchmark.class.getResourceAsStream("/wordpress.json"));
        }

        public InputStream smallBinaryTestData() {
            return new BufferedInputStream(WsonParserBenchmark.class.getResourceAsStream("/wordpress.jbon"));
        }
    }

    private static Value.Configuration buildConfiguration() {
        return Value.Configuration.builder()
                .objectValueImplementation(ObjectValue.Implementation.ArrayList)
                .build();
    }

    @SneakyThrows
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void antlr(ExecutionPlan plan) {
        try (InputStream is = plan.smallTestData()) {
            CharStream inputStream = CharStreams.fromReader(new InputStreamReader(is));
            JSONLexer lexer = new JSONLexer(inputStream);
            CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
            net.woggioni.wson.antlr.JSONParser parser = new net.woggioni.wson.antlr.JSONParser(commonTokenStream);
            JSONListenerImpl listener = new JSONListenerImpl();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, parser.json());
        }
    }

    @SneakyThrows
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void worthJson(ExecutionPlan plan) {
        try (InputStream is = plan.smallTestData()) {
            new JSONParser(buildConfiguration()).parse(is);
        }
    }

    @SneakyThrows
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void worthJbon(ExecutionPlan plan) {
        try (InputStream is = plan.smallBinaryTestData()) {
            new JBONParser(buildConfiguration()).parse(is);
        }
    }

    @SneakyThrows
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void worthJbonHuge(ExecutionPlan plan) {
        try (InputStream is = plan.hugeBinaryTestData()) {
            new JBONParser(buildConfiguration()).parse(is);
        }
    }

    @SneakyThrows
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void worthHuge(ExecutionPlan plan) {
        try (InputStream is = plan.hugeTestData()) {
            new JSONParser(buildConfiguration()).parse(is);
        }
    }

    @SneakyThrows
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void jackson(ExecutionPlan plan) {
        try (InputStream is = plan.smallTestData()) {
            ObjectMapper om = new ObjectMapper();
            om.readTree(new InputStreamReader(is));
        }
    }

    @SneakyThrows
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void jacksonHuge(ExecutionPlan plan) {
        try (InputStream is = plan.hugeTestData()) {
            ObjectMapper om = new ObjectMapper();
            om.readTree(new InputStreamReader(is));
        }
    }
}