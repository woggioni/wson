package net.woggioni.wson.cli

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess
import com.beust.jcommander.IStringConverter
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.beust.jcommander.converters.PathConverter
import net.woggioni.wson.serialization.binary.JBONDumper
import net.woggioni.wson.serialization.binary.JBONParser
import net.woggioni.wson.serialization.json.JSONDumper
import net.woggioni.wson.serialization.json.JSONParser
import net.woggioni.wson.xface.Value


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

private class OutputTypeConverter : IStringConverter<SerializationFormat> {
    override fun convert(value: String): SerializationFormat = SerializationFormat.parse(value)
}

private class CliArg {

    @Parameter(names = ["-f", "--file"], description = "Name of the input file to parse", converter = PathConverter::class)
    var fileName: Path? = null

    @Parameter(names = ["--input-type"], description = "Input type", converter = OutputTypeConverter::class)
    var inputType: SerializationFormat = SerializationFormat.JSON

    @Parameter(names = ["-o", "--output"], description = "Name of the JSON file to generate", converter = PathConverter::class)
    var output: Path? = null

    @Parameter(names = ["-t", "--type"], description = "Output type", converter = OutputTypeConverter::class)
    var outputType: SerializationFormat = SerializationFormat.JSON

    @Parameter(names = ["-h", "--help"], help = true)
    var help: Boolean = false
}

fun main(vararg args: String) {
    val cliArg = CliArg()
    val cliArgumentParser = JCommander.newBuilder()
            .addObject(cliArg)
            .build()
    try {
        cliArgumentParser.parse(*args)
    } catch (pe: ParameterException) {
        cliArgumentParser.usage()
        exitProcess(-1)
    }
    if (cliArg.help) {
        cliArgumentParser.usage()
        exitProcess(0)
    }
    val cfg = Value.Configuration.builder().serializeReferences(true).build()
    val inputStream = if (cliArg.fileName != null) {
        BufferedInputStream(Files.newInputStream(cliArg.fileName))
    } else {
        System.`in`
    }

    val result = when(cliArg.inputType) {
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

    val outputStream = if (cliArg.output != null) {
        BufferedOutputStream(Files.newOutputStream(cliArg.output))
    } else {
        System.out
    }
    when(cliArg.outputType) {
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