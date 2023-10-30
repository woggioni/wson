package net.woggioni.wson.cli;

import lombok.SneakyThrows;
import net.woggioni.jwo.Fun;
import net.woggioni.wson.serialization.binary.JBONDumper;
import net.woggioni.wson.serialization.binary.JBONParser;
import net.woggioni.wson.serialization.json.JSONDumper;
import net.woggioni.wson.serialization.json.JSONParser;
import net.woggioni.wson.xface.Value;
import picocli.CommandLine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static net.woggioni.jwo.JWO.newThrowable;


@CommandLine.Command(
        name = "wson",
        versionProvider = VersionProvider.class)
public class WsonCommand implements Runnable {

    @CommandLine.Option(
            names = {"-f", "--file"},
            description = {"Name of the input file to parse"}
    )
    private Path fileName;

    @CommandLine.Option(
            names = {"--input-type"},
            description = {"Input type"},
            converter = {SerializationTypeConverter.class})
    private SerializationFormat inputType = SerializationFormat.JSON;

    @CommandLine.Option(names = {"-o", "--output"},
            description = {"Name of the JSON file to generate"})
    private Path output;

    @CommandLine.Option(
            names = {"-t", "--type"},
            description = {"Output type"},
            converter = {SerializationTypeConverter.class})
    private SerializationFormat outputType = SerializationFormat.JSON;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help = false;

    @SneakyThrows
    public void run() {
        Value.Configuration cfg = Value.Configuration.builder().serializeReferences(true).build();
        InputStream inputStream;
        if (fileName != null) {
            inputStream = new BufferedInputStream(Files.newInputStream(fileName));
        } else {
            inputStream = System.in;
        }

        Value result;
        switch (inputType) {
            case JSON:
                try (Reader reader = new InputStreamReader(inputStream)) {
                    result = new JSONParser(cfg).parse(reader);
                }
                break;
            case JBON:
                try {
                    result = new JBONParser(cfg).parse(inputStream);
                } finally {
                    inputStream.close();
                }
                break;
            default:
                throw newThrowable(RuntimeException.class, "This sohuld never happen");
        }

        OutputStream outputStream = Optional.ofNullable(output)
                .map((Fun<Path, OutputStream>) Files::newOutputStream)
                .<OutputStream>map(BufferedOutputStream::new)
                .orElse(System.out);

        switch (outputType) {
            case JSON:
                try (Writer writer = new OutputStreamWriter(outputStream)) {
                    new JSONDumper(cfg).dump(result, writer);
                }
                break;
            case JBON:
                try {
                    new JBONDumper(cfg).dump(result, outputStream);
                } finally {
                    outputStream.close();
                }
        }
    }
}
