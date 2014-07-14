package org.jboss.windup.util;

/**
 * 
 * @author jsightler
 * 
 */
public class FilenameUtil
{
    /**
     * Conservative approach to insuring that a given filename only contains characters that are legal for use in
     * filenames on the disk.
     * 
     * @param badFileName
     * @return
     */
    public static String cleanFileName(String badFileName)
    {
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < badFileName.length(); i++)
        {
            int c = (int) badFileName.charAt(i);
            if (Character.isJavaIdentifierPart(c))
            {
                cleanName.append((char) c);
            }
        }
        return cleanName.toString();
    }
}
