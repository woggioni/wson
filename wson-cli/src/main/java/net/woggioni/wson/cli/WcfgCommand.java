package net.woggioni.wson.cli;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.woggioni.jwo.Fun;
import net.woggioni.wson.serialization.binary.JBONDumper;
import net.woggioni.wson.serialization.json.JSONDumper;
import net.woggioni.wson.value.ObjectValue;
import net.woggioni.wson.wcfg.WConfig;
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


@CommandLine.Command(
        name = "wcfg",
        versionProvider = VersionProvider.class)
public class WcfgCommand implements Runnable {

    @CommandLine.Option(
            names = {"-f", "--file"},
            description = {"Name of the input file to parse"}
    )
    private Path fileName;

    @CommandLine.Option(names = {"-o", "--output"},
            description = {"Name of the JSON file to generate"})
    private Path output;

    @CommandLine.Option(
            names = {"-t", "--type"},
            description = {"Output type"},
            converter = {SerializationTypeConverter.class})
    private SerializationFormat outputType = SerializationFormat.JSON;

    @CommandLine.Option(names = {"-d", "--resolve-refernces"}, description = "Resolve references")
    private boolean resolveReferences = false;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help = false;

    @SneakyThrows
    public void run() {
        Value.Configuration cfg = Value.Configuration.builder()
                .objectValueImplementation(ObjectValue.Implementation.LinkedHashMap)
                .serializeReferences(!resolveReferences)
                .build();
        InputStream inputStream;
        if (fileName != null) {
            inputStream = new BufferedInputStream(Files.newInputStream(fileName));
        } else {
            inputStream = System.in;
        }

        Value result;
        try(Reader reader = new InputStreamReader(inputStream)) {
            WConfig wcfg = WConfig.parse(reader, cfg);
            result = wcfg.whole();
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
