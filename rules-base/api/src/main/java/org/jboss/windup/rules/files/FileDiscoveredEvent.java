package org.jboss.windup.rules.files;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface FileDiscoveredEvent
{
    String getFilename();

    InputStream getInputStream() throws IOException;
}
