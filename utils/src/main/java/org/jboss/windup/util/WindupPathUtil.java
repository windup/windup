package org.jboss.windup.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides useful methods for manipulating filenames (eg, removing illegal chars from files).
 *
 * @author jsightler
 */
public class WindupPathUtil
{
    /**
     * The path ~/.windup
     */
    public static Path getWindupUserDir()
    {
        String userHome = System.getProperty("user.home");
        if (userHome == null)
            return null;
        return Paths.get(userHome).resolve(".windup");
    }

    /**
     * The path ~/.windup/rules
     */
    public static Path getWindupUserRulesDir()
    {
        Path windupUserDir = getWindupUserDir();
        if (windupUserDir == null)
            return null;
        return windupUserDir.resolve("rules");
    }

    /**
     * The path ~/.windup/ignore
     */
    public static Path getWindupIgnoreListDir()
    {
        Path windupUserDir = getWindupUserDir();
        if (windupUserDir == null)
            return null;
        return windupUserDir.resolve("ignore");
    }

    /**
     * The path WINDUP_HOME
     */
    public static Path getWindupHome()
    {
        String forgeHome = System.getProperty("forge.home");
        if (forgeHome == null)
            return null;
        return Paths.get(forgeHome);
    }

    /**
     * The path WINDUP_HOME/rules
     */
    public static Path getWindupHomeRules()
    {
        Path windupHome = getWindupHome();
        if (windupHome == null)
            return null;
        return windupHome.resolve("rules");
    }

    /**
     * The path WINDUP_HOME/ignore
     */
    public static Path getWindupHomeIgnoreListDir()
    {
        Path windupHome = getWindupHome();
        if (windupHome == null)
            return null;
        return windupHome.resolve("ignore");
    }

    /**
     * Conservative approach to insuring that a given filename only contains characters that are legal for use in
     * filenames on the disk. Other characters are replaced with underscore _ .
     */
    public static String cleanFileName(String badFileName)
    {
        if (badFileName == null)
            return null;

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
     * Converts a path to a class file (like "foo/bar/My.class" or "foo\\bar\\My.class") to a fully qualified class name
     * (like "foo.bar.My").
     */
    public static String classFilePathToClassname(String classFilePath)
    {
        if (classFilePath == null)
            return null;

        final int pos = classFilePath.lastIndexOf(".class");
        if (pos < 0)
            throw new IllegalArgumentException("Not a .class file path: " + classFilePath);

        return classFilePath.substring(0, pos).replace('/', '.').replace('\\', '.');
    }
}
