package org.jboss.windup.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.forge.furnace.util.OperatingSystemUtils;

/**
 * Provides useful methods for manipulating filenames (eg, removing illegal chars from files).
 *
 * @author jsightler
 */
public class WindupPathUtil
{
    public static Path getWindupUserDir()
    {
        return OperatingSystemUtils.getUserHomeDir().toPath().resolve(".windup");
    }

    public static Path getWindupUserRulesDir()
    {
        return getWindupUserDir().resolve("rules");
    }
    
    public static Path getWindupIgnoreListDir()
    {
        return getWindupUserDir().resolve("ignore");
    }

    public static Path getWindupHome()
    {
        return Paths.get(System.getProperty("forge.home"));
    }

    public static Path getWindupHomeRules()
    {
        return getWindupHome().resolve("rules");
    }
    
    public static Path getWindupHomeIgnoreListDir()
    {
        return getWindupHome().resolve("ignore");
    }

    /**
     * Conservative approach to insuring that a given filename only contains characters that are legal for use in filenames on the disk. Other
     * characters are replaced with _ .
     */
    public static String cleanFileName(String badFileName)
    {
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < badFileName.length(); i++)
        {
            int c = (int) badFileName.charAt(i);
            if (Character.isJavaIdentifierPart(c))
                cleanName.append((char) c);
            else
                cleanName.append('_');
        }
        return cleanName.toString();
    }

    /**
     * Converts a path to a class file (like "foo/bar/My.class" or "foo\\bar\\My.class") to a fully qualified class name (like "foo.bar.My").
     */
    public static String classFilePathToClassname(String classFilePath)
    {
        final int pos = classFilePath.lastIndexOf(".class");
        if (pos < 0)
            throw new IllegalArgumentException("Not a .class file path: " + classFilePath);

        return classFilePath.substring(0, pos).replace('/', '.').replace('\\', '.');
    }
}
