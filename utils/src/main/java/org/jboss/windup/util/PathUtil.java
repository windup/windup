package org.jboss.windup.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

/**
 * Provides useful methods for manipulating filenames (eg, removing illegal chars from files).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class PathUtil
{
    private static final Logger LOG = Logger.getLogger(PathUtil.class.getName());

    public static final String WINDUP_HOME = "windup.home";
    public static final String WINDUP_RULESETS_DIR_SYSPROP = "windup.rulesets.dir";

    public static final String RULES_DIRECTORY_NAME = "rules";
    public static final String IGNORE_DIRECTORY_NAME = "ignore";
    public static final String CACHE_DIRECTORY_NAME = "cache";
    public static final String ADDONS_DIRECTORY_NAME = "addons";
    public static String LIBRARY_DIRECTORY_NAME = "lib";
    public static String BINARY_DIRECTORY_NAME = "bin";

    /**
     * The path $USER_HOME/.windup
     */
    public static Path getWindupUserDir()
    {
        String userHome = System.getProperty("user.home");
        if (userHome == null)
        {
            Path path = new File("").toPath();
            LOG.warning("$USER_HOME not set, using [" + path.toAbsolutePath().toString() + "] instead.");
            return path;
        }
        return Paths.get(userHome).resolve(".windup");
    }

    /**
     * The path $WINDUP_HOME (where Windup is installed.)
     */
    public static Path getWindupHome()
    {
        String windupHome = System.getProperty(WINDUP_HOME);
        if (windupHome == null)
        {
            Path path = new File("").toPath();
            LOG.warning("$WINDUP_HOME not set, using [" + path.toAbsolutePath().toString() + "] instead.");
            return path;
        }
        return Paths.get(windupHome);
    }

    public static void setWindupHome(Path windupHome)
    {
        System.setProperty(WINDUP_HOME, windupHome.toAbsolutePath().toString());
    }

    /**
     * The path $USER_HOME/cache
     */
    public static Path getUserCacheDir()
    {
        return getUserSubdirectory(CACHE_DIRECTORY_NAME);
    }

    /**
     * The path $WINDUP_HOME/cache
     */
    public static Path getWindupCacheDir()
    {
        return getWindupSubdirectory(CACHE_DIRECTORY_NAME);
    }

    /**
     * The path $USER_HOME/ignore
     */
    public static Path getUserIgnoreDir()
    {
        return getUserSubdirectory(IGNORE_DIRECTORY_NAME);
    }

    /**
     * The path $WINDUP_HOME/ignore
     */
    public static Path getWindupIgnoreDir()
    {
        return getWindupSubdirectory(IGNORE_DIRECTORY_NAME);
    }

    /**
     * The path $WINDUP_HOME/addons
     */
    public static Path getWindupAddonsDir()
    {
        return getWindupSubdirectory(ADDONS_DIRECTORY_NAME);
    }

    /**
     * The path $USER_HOME/rules
     */
    public static Path getUserRulesDir()
    {
        return getUserSubdirectory(RULES_DIRECTORY_NAME);
    }

    /**
     * The path $WINDUP_HOME/rules
     */
    public static Path getWindupRulesDir()
    {
        String rulesDir = System.getProperty(WINDUP_RULESETS_DIR_SYSPROP);
        if (rulesDir != null)
        {
            Path path = Paths.get(rulesDir);
            if (!path.toFile().exists())
                LOG.warning(WINDUP_RULESETS_DIR_SYSPROP + " points to a non-existent directory!" + path.toAbsolutePath().toString());
            return path;
        }
        else
            return getWindupSubdirectory(RULES_DIRECTORY_NAME);
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

    /**
     * Returns the root path for this source file, based upon the package name.
     *
     * For example, if path is "/project/src/main/java/org/example/Foo.java" and the package is "org.example", then this
     * should return "/project/src/main/java".
     *
     * Returns null if the folder structure does not match the package name.
     */
    public static Path getRootFolderForSource(Path sourceFilePath, String packageName)
    {
        if (packageName == null || packageName.trim().equals(""))
        {
            return sourceFilePath.getParent();
        }
        String[] packageNameComponents = packageName.split("\\.");
        Path currentPath = sourceFilePath.getParent();
        for (int i = packageNameComponents.length; i > 0; i--)
        {
            String packageComponent = packageNameComponents[i - 1];
            if (!StringUtils.equals(packageComponent, currentPath.getFileName().toString()))
            {
                return null;
            }
            currentPath = currentPath.getParent();
        }
        return currentPath;
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
