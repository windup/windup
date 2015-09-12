package org.jboss.windup.rules.files;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DefaultFileDiscoveredEvent implements FileDiscoveredEvent
{
    private Path path;

    public DefaultFileDiscoveredEvent(String pathString)
    {
        this.path = Paths.get(pathString);
    }

    @Override
    public String getFilename()
    {
        return path.getFileName().toString();
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return new FileInputStream(this.path.toFile());
    }
}
