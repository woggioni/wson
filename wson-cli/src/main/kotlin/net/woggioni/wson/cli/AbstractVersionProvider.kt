package net.woggioni.wson.cli

import java.net.URL
import java.util.Enumeration
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.Manifest
import lombok.SneakyThrows
import picocli.CommandLine


abstract class AbstractVersionProvider @SneakyThrows protected constructor(specificationTitle: String?) :
    CommandLine.IVersionProvider {
    private val version: String
    private val vcsHash: String

    init {
        var version: String? = null
        var vcsHash: String? = null
        val it: Enumeration<URL> = javaClass.classLoader.getResources(JarFile.MANIFEST_NAME)
        while (it.hasMoreElements()) {
            val manifestURL: URL = it.nextElement()
            val mf = Manifest()
            manifestURL.openStream().use(mf::read)
            val mainAttributes: Attributes = mf.mainAttributes
            if (specificationTitle == mainAttributes.getValue(Attributes.Name.SPECIFICATION_TITLE)) {
                version = mainAttributes.getValue(Attributes.Name.SPECIFICATION_VERSION)
                vcsHash = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION)
            }
        }
        if (version == null || vcsHash == null) {
            throw RuntimeException("Version information not found in manifest")
        }
        this.version = version
        this.vcsHash = vcsHash
    }

    override fun getVersion(): Array<String?> {
        return if (version.endsWith("-SNAPSHOT")) {
            arrayOf(version, vcsHash)
        } else {
            arrayOf(version)
        }
    }
}