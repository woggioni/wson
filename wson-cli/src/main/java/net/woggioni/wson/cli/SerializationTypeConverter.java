package net.woggioni.wson.cli;

import picocli.CommandLine;

class SerializationTypeConverter implements CommandLine.ITypeConverter<SerializationFormat> {
    @Override
    public SerializationFormat convert(String value) {
        return SerializationFormat.parse(value);
    }
}
