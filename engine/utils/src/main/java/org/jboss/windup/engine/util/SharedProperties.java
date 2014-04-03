package org.jboss.windup.engine.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SharedProperties
{
    public static Path getWindupConfigurationDirectory() {
        return Paths.get(System.getProperty("user.home"), ".windup");
    }
}
