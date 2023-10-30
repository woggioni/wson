package net.woggioni.wson.cli;

import lombok.SneakyThrows;
import picocli.CommandLine;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;


abstract class AbstractVersionProvider implements CommandLine.IVersionProvider {
    private String version;
    private String vcsHash;

    @SneakyThrows
    protected AbstractVersionProvider(String specificationTitle) {
        String version = null;
        String vcsHash = null;
        Enumeration<URL> it = getClass().getClassLoader().getResources(JarFile.MANIFEST_NAME);
        while (it.hasMoreElements()) {
            URL manifestURL = it.nextElement();
            Manifest mf = new Manifest();
            try (InputStream inputStream = manifestURL.openStream()) {
                mf.read(inputStream);
            }
            Attributes mainAttributes = mf.getMainAttributes();
            if (Objects.equals(specificationTitle, mainAttributes.getValue(Attributes.Name.SPECIFICATION_TITLE))) {
                version = mainAttributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
                vcsHash = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            }
        }
        if (version == null || vcsHash == null) {
            throw new RuntimeException("Version information not found in manifest");
        }
        this.version = version;
        this.vcsHash = vcsHash;
    }

    @Override
    public String[] getVersion() {
        if (version.endsWith("-SNAPSHOT")) {
            return new String[]{version, vcsHash};
        } else {
            return new String[]{version};
        }
    }
}