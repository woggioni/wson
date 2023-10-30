package net.woggioni.wson.cli

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess
import net.woggioni.wson.serialization.binary.JBONDumper
import net.woggioni.wson.serialization.binary.JBONParser
import net.woggioni.wson.serialization.json.JSONDumper
import net.woggioni.wson.serialization.json.JSONParser
import net.woggioni.wson.xface.Value
import org.slf4j.LoggerFactory
import picocli.CommandLine


sealed class SerializationFormat(val name: String) {
    override fun toString() = name

    object JSON : SerializationFormat("json")
    object JBON : SerializationFormat("jbon")

    companion object {
        fun parse(value: String) = when (value) {
            JBON.name -> JBON
            JSON.name -> JSON
            else -> {
                val availableValues = sequenceOf(
                        JSON,
                        JBON
                ).map(SerializationFormat::name).joinToString(", ")
                throw IllegalArgumentException(
                        "Unknown serialization format '$value', possible values are $availableValues")
            }
        }
    }
}

class OutputTypeConverter : CommandLine.ITypeConverter<SerializationFormat> {
    override fun convert(value: String): SerializationFormat = SerializationFormat.parse(value)
}


class VersionProvider internal constructor() : AbstractVersionProvider("wson-cli")

@CommandLine.Command(
    name = "wson-cli",
    versionProvider = VersionProvider::class)
private class WsonCli : Runnable {

    @CommandLine.Option(
        names = ["-f", "--file"],
        description = ["Name of the input file to parse"],
    )
    var fileName: Path? = null

    @CommandLine.Option(
        names = ["--input-type"],
        description = ["Input type"],
        converter = [OutputTypeConverter::class])
    var inputType: SerializationFormat = SerializationFormat.JSON

    @CommandLine.Option(names = ["-o", "--output"],
        description = ["Name of the JSON file to generate"])
    var output: Path? = null

    @CommandLine.Option(
        names = ["-t", "--type"],
        description = ["Output type"],
        converter = [OutputTypeConverter::class])
    var outputType: SerializationFormat = SerializationFormat.JSON

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    var help: Boolean = false

    override fun run() {
        val cfg = Value.Configuration.builder().serializeReferences(true).build()
        val inputStream = if (fileName != null) {
            BufferedInputStream(Files.newInputStream(fileName))
        } else {
            System.`in`
        }

        val result = when(inputType) {
            SerializationFormat.JSON -> {
                val reader = InputStreamReader(inputStream)
                try {
                    JSONParser(cfg).parse(reader)
                } finally {
                    reader.close()
                }
            }
            SerializationFormat.JBON -> {
                try {
                    JBONParser(cfg).parse(inputStream)
                } finally {
                    inputStream.close()
                }
            }
        }

        val outputStream = output?.let {
            BufferedOutputStream(Files.newOutputStream(it))
        } ?: System.out
        when(outputType) {
            SerializationFormat.JSON -> {
                val writer = OutputStreamWriter(outputStream)
                try {
                    JSONDumper(cfg).dump(result, writer)
                } finally {
                    writer.close()
                }
            }
            SerializationFormat.JBON -> {
                try {
                    JBONDumper(cfg).dump(result, outputStream)
                } finally {
                    outputStream.close()
                }
            }
        }
    }
}


fun main(vararg args: String) {
    val log = LoggerFactory.getLogger("wson-cli")
    val commandLine = CommandLine(WsonCli())
    commandLine.setExecutionExceptionHandler { ex, cl, parseResult ->
        log.error(ex.message, ex)
        CommandLine.ExitCode.SOFTWARE
    }
    exitProcess(commandLine.execute(*args))
}