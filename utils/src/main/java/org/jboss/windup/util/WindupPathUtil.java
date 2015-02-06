package org.jboss.windup.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Provides useful methods for manipulating filenames (eg, removing illegal chars from files).
 *
 * @author jsightler
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class WindupPathUtil
{
    private static final Logger log = Logger.getLogger(WindupPathUtil.class.getName());

    /**
     * The path $USER_HOME/.windup
     */
    public static Path getWindupUserDir()
    {
        String userHome = System.getProperty("user.home");
        if (userHome == null)
        {
            Path path = new File("").toPath();
            log.warning("$USER_HOME not set, using [" + path + "] instead.");
            return path;
        }
        return Paths.get(userHome).resolve(".windup");
    }

    /**
     * The path $WINDUP_HOME (where Windup is installed.)
     */
    public static Path getWindupHome()
    {
        String windupHome = System.getProperty("windup.home");
        if (windupHome == null)
        {
            Path path = new File("").toPath();
            log.warning("$WINDUP_HOME not set, using [" + path + "] instead.");
            return path;
        }
        return Paths.get(windupHome);
    }

    /**
     * The path $USER_HOME/cache
     */
    public static Path getUserCacheDir()
    {
        return getUserSubdirectory("cache");
    }

    /**
     * The path $WINDUP_HOME/cache
     */
    public static Path getWindupCacheDir()
    {
        return getWindupSubdirectory("cache");
    }

    /**
     * The path $USER_HOME/ignore
     */
    public static Path getUserIgnoreDir()
    {
        return getUserSubdirectory("ignore");
    }

    /**
     * The path $WINDUP_HOME/ignore
     */
    public static Path getWindupIgnoreDir()
    {
        return getWindupSubdirectory("ignore");
    }

    /**
     * The path $USER_HOME/rules
     */
    public static Path getUserRulesDir()
    {
        return getUserSubdirectory("rules");
    }

    /**
     * The path $WINDUP_HOME/rules
     */
    public static Path getWindupRulesDir()
    {
        return getWindupSubdirectory("rules");
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

    /*
     * Helpers
     */
    private static Path getUserSubdirectory(String subdirectory)
    {
        Path windupUserDir = getWindupUserDir();
        if (windupUserDir == null)
            return null;
        return windupUserDir.resolve(subdirectory);
    }

    private static Path getWindupSubdirectory(String subdirectory)
    {
        Path windupHome = getWindupHome();
        if (windupHome == null)
            return null;
        return windupHome.resolve(subdirectory);
    }
}
