package org.jeromerodrigo.lucidengine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to hold various resource loading logic.
 *
 * @author Jerome Edward
 */

public final class ResourceLoader {

    private static final Logger LOG = LogManager
            .getLogger(ResourceLoader.class);

    public static final File ROOT = new File(".");

    private ResourceLoader() {

    }

    /**
     * Creates a File object based on provided reference.
     *
     * @param ref
     *            the reference string
     * @return File
     */

    private static File createFile(final String ref) {
        File file = new File(ROOT, ref);
        if (!file.exists()) {
            file = new File(ref);
        }
        return file;
    }

    /**
     * Gets an InputStream from specified resource location reference.
     *
     * @param ref
     *            the reference string
     * @return InputStream of the resource
     */

    public static InputStream getResourceAsStream(final String ref) {
        final InputStream in = ResourceLoader.class.getClassLoader()
                .getResourceAsStream(ref);
        if (in == null) { // try file system
            try {
                return new FileInputStream(createFile(ref));
            } catch (final IOException e) {
                LOG.fatal(ref + "\n" + e.getMessage());
            }
        }
        return in;
    }

    /**
     * Gets a URL of the specified resource location reference.
     *
     * @param ref
     *            the reference string
     * @return URL of the specified resource
     */

    public static URL getResource(final String ref) {
        final URL url = Thread.currentThread().getContextClassLoader()
                .getResource(ref);
        if (url == null) {
            try {
                final File f = createFile(ref);
                if (f.exists()) {
                    return f.toURI().toURL();
                }
            } catch (final IOException e) {
                LOG.fatal(ref + "\n" + e.getMessage());
            }
        }
        return url;
    }
}
