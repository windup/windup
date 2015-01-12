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
