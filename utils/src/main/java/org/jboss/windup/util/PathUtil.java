package org.jboss.windup.util;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import org.jboss.windup.util.exception.WindupException;

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
    public static final String WINDUP_LABELSETS_DIR_SYSPROP = "windup.labelsets.dir";

    public static final String RULES_DIRECTORY_NAME = "rules";
    public static final String LABELS_DIRECTORY_NAME = "labels";
    public static final String IGNORE_DIRECTORY_NAME = "ignore";
    public static final String CACHE_DIRECTORY_NAME = "cache";
    public static final String ADDONS_DIRECTORY_NAME = "addons";
    public static String LIBRARY_DIRECTORY_NAME = "lib";
    public static String BINARY_DIRECTORY_NAME = "bin";

    /**
     * The path $USER_HOME/.mta
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
        return Paths.get(userHome).resolve(".mta");
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
     * The path $USER_HOME/labels
     */
    public static Path getUserLabelsDir()
    {
        return getUserSubdirectory(LABELS_DIRECTORY_NAME);
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
     * The path $WINDUP_HOME/labels
     */
    public static Path getWindupLabelsDir()
    {
        String labelsDir = System.getProperty(WINDUP_LABELSETS_DIR_SYSPROP);
        if (labelsDir != null)
        {
            Path path = Paths.get(labelsDir);
            if (!path.toFile().exists())
                LOG.warning(WINDUP_LABELSETS_DIR_SYSPROP + " points to a non-existent directory!" + path.toAbsolutePath().toString());
            return path;
        }
        else
            return getWindupSubdirectory(LABELS_DIRECTORY_NAME);
    }

    /**
     * Conservative approach to insuring that a given filename only contains characters that are legal for use in
     * filenames on the disk. Other characters are replaced with underscore _ .
     * Note that this should only be used with the filename itself, not the entire path, because it removes the '/' characters as well.
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
    public static String classFilePathToClassname(String relativePath)
    {
        if (relativePath == null)
            return null;

        final int pos = relativePath.lastIndexOf(".class");
        if (pos < 0 && relativePath.lastIndexOf(".java") < 0)
            throw new IllegalArgumentException("Not a .class/.java file path: " + relativePath);

        relativePath = FilenameUtils.separatorsToUnix(relativePath);

        if (relativePath.startsWith("/"))
        {
            relativePath = relativePath.substring(1);
        }

        if (relativePath.startsWith("src/main/java/"))
        {
            relativePath = relativePath.substring("src/main/java/".length());
        }

        if (relativePath.startsWith("WEB-INF/classes/"))
        {
            relativePath = relativePath.substring("WEB-INF/classes/".length());
        }

        if (relativePath.startsWith("WEB-INF/classes.jdk15/"))
        {
            relativePath = relativePath.substring("WEB-INF/classes.jdk15/".length());
        }

        if (relativePath.endsWith(".class"))
        {
            relativePath = relativePath.substring(0, relativePath.length() - ".class".length());
        }
        else if (relativePath.endsWith(".java"))
        {
            relativePath = relativePath.substring(0, relativePath.length() - ".java".length());
        }

        String qualifiedName = relativePath.replace("/", ".");
        return qualifiedName;
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
        if (packageName == null || packageName.trim().isEmpty())
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

    /**
     * Returns true if "file" is a subfile or subdirectory of "dir".
     *
     * For example with the directory /path/to/a, the following return values would occur:
     *
     * /path/to/a/foo.txt - true /path/to/a/bar/zoo/boo/team.txt - true /path/to/b/foo.txt - false
     *
     */
    public static boolean isInSubDirectory(File dir, File file)
    {
        if (file == null)
            return false;

        if (file.equals(dir))
            return true;

        return isInSubDirectory(dir, file.getParentFile());
    }

    /**
     * Attempts to convert a path name (possibly a path within an archive) to a package name.
     */
    public static String pathToPackageName(String relativePath)
    {
        String qualifiedName = classFilePathToClassname(relativePath);
        return ClassNameUtil.getPackageName(qualifiedName);
    }

    /**
     * Creates the given directory. Fails if it already exists.
     */
    public static void createDirectory(Path dir, String dirDesc)
    {
        try
        {
            Files.createDirectories(dir);
        }
        catch (IOException ex)
        {
            throw new WindupException("Error creating " + dirDesc + " folder: " + dir.toString() + " due to: " + ex.getMessage(), ex);
        }
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
