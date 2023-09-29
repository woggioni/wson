package net.woggioni.wson.serialization.binary;

import lombok.SneakyThrows;
import net.woggioni.wson.serialization.json.JSONParser;
import net.woggioni.wson.value.ObjectValue;
import net.woggioni.wson.xface.Parser;
import net.woggioni.wson.xface.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class JBONTest {

    private static String[] testFiles = new String[]{"/test.json", "/wordpress.json"};

    private static InputStream getTestSource(String filename) {
        return JBONTest.class.getResourceAsStream(filename);
    }

    @TempDir
    Path testDir;

    @Test
    @SneakyThrows
    public void consistencyTest() {
        Value.Configuration cfg = Value.Configuration.builder()
                .objectValueImplementation(ObjectValue.Implementation.TreeMap).build();
        for (String testFile : testFiles) {
            Value parsedValue;
            try (InputStream is = getTestSource(testFile)) {
                Parser parser = new JSONParser(cfg);
                parsedValue = parser.parse(is);
            }
            byte[] dumpedJBON;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                JBONDumper.newInstance().dump(parsedValue, baos);
                dumpedJBON = baos.toByteArray();
            }
            Value reParsedValue;
            try (InputStream is = new ByteArrayInputStream(dumpedJBON)) {
                Parser parser = new JBONParser(cfg);
                reParsedValue = parser.parse(is);
            }
            Assertions.assertEquals(parsedValue, reParsedValue);
            byte[] reDumpedJBON;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                JBONDumper.newInstance().dump(reParsedValue, baos);
                reDumpedJBON = baos.toByteArray();
            }
            Assertions.assertArrayEquals(dumpedJBON, reDumpedJBON);
        }
    }

    @Test
    @SneakyThrows
    public void comparativeTest() {
        for (String testFile : testFiles) {
            Value originalValue = new JSONParser().parse(getTestSource(testFile));

            Path outputFile = Files.createTempFile(testDir, "worth", null);
            try (OutputStream os = new FileOutputStream(outputFile.toFile())) {
                JBONDumper jbonDumper = new JBONDumper();
                jbonDumper.dump(originalValue, os);
            }
            Value binarySerializedValue;
            try (InputStream is = new FileInputStream(outputFile.toFile())) {
                JBONParser jbonParser = new JBONParser();
                binarySerializedValue = jbonParser.parse(is);
            }
            Assertions.assertEquals(originalValue, binarySerializedValue);
        }
    }
}
