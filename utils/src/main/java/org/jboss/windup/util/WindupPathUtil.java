package org.jboss.windup.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.jboss.forge.furnace.util.OperatingSystemUtils;

/**
 * Provides useful methods for manipulating filenames (eg, removing illegal chars from files).
 *
 * @author jsightler
 */
public class WindupPathUtil
{
    /**
     * The path ~/.windup
     * 
     * @return
     */
    public static Path getWindupUserDir()
    {
        return OperatingSystemUtils.getUserHomeDir().toPath().resolve(".windup");
    }

    /**
     * The path ~/.windup/rules
     * 
     * @return
     */
    public static Path getWindupUserRulesDir()
    {
        return getWindupUserDir().resolve("rules");
    }

    /**
     * The path ~/.windup/ignore
     * 
     * @return
     */
    public static Path getWindupIgnoreListDir()
    {
        return getWindupUserDir().resolve("ignore");
    }

    /**
     * The path WINDUP_HOME (== FORGE_HOME)
     * 
     * @return
     */
    public static Path getWindupHome()
    {
        return Paths.get(System.getProperty("forge.home"));
    }

    /**
     * The path WINDUP_HOME/rules
     * 
     * @return
     */
    public static Path getWindupHomeRules()
    {
        return getWindupHome().resolve("rules");
    }

    /**
     * The path WINDUP_HOME/ignore
     * 
     * @return
     */
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

    public static String pathToPackagename(String relativePath)
    {
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

        if (relativePath.endsWith(".class"))
        {
            relativePath = relativePath.substring(0, relativePath.length() - ".class".length());
        }
        else if (relativePath.endsWith(".java"))
        {
            relativePath = relativePath.substring(0, relativePath.length() - ".java".length());
        }

        String qualifiedName = relativePath.replace("/", ".");
        String packageName = "";
        if (qualifiedName.contains("."))
            packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));

        return packageName;
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

    public static boolean pathNotEmpty(Path p)
    {
        File f = p.toFile();
        if (f.exists() && !f.isDirectory())
        {
            return true;
        }
        if (f.isDirectory() && f.listFiles() != null && f.listFiles().length > 0)
        {
            return true;
        }
        return false;
    }
}
