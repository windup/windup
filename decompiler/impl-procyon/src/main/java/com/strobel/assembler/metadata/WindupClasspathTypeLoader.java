package com.strobel.assembler.metadata;

import com.strobel.core.VerifyArgument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:marcorizzi82@gmail.com>Marco Rizzi</a>
 */
public class WindupClasspathTypeLoader implements ITypeLoader {
    private final static Logger LOG = Logger.getLogger(WindupClasspathTypeLoader.class.getSimpleName());

    private final URLClassLoader urlClassLoader;

    public WindupClasspathTypeLoader(final String classPath) {
        final String[] parts = VerifyArgument.notNull(classPath, "classPath")
                .split(Pattern.quote(System.getProperty("path.separator")));

        final URL[] urls = Arrays.stream(parts).map(path -> {
            try {
                return new File(path).toURI().toURL();
            } catch (MalformedURLException e) {
                throw new UndeclaredThrowableException(e);
            }
        }).toArray(URL[]::new);

        urlClassLoader = new URLClassLoader(urls);
    }

    @Override
    public boolean tryLoadType(String internalName, Buffer buffer) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Attempting to load type: " + internalName + "...");
        }

        final String path = internalName.concat(".class");
        final URL resource = urlClassLoader.getResource(path);

        if (resource == null) {
            return false;
        }

        try (final InputStream stream = urlClassLoader.getResourceAsStream(path)) {
            final byte[] temp = new byte[4096];

            int bytesRead;

            while ((bytesRead = stream.read(temp, 0, temp.length)) > 0) {
                buffer.ensureWriteableBytes(bytesRead);
                buffer.putByteArray(temp, 0, bytesRead);
            }

            buffer.flip();

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Type loaded from " + resource + ".");
            }

            return true;
        } catch (final IOException ignored) {
            return false;
        }
    }
}
