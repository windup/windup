package org.jboss.windup.util;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 */
public class ClassNameUtil
{
    public static final String getPackageName(String qualifiedName)
    {
        String packageName = "";
        if (qualifiedName.contains("."))
            packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
        return packageName;
    }
    
    /*
     * This method assumes constants are upper cased
     */
    public static boolean isConstant(String fqcn)
    {
        return isUpperCase(fqcn);
    }
    
    public static boolean isUpperCase(String s)
    {
        for (int i=0; i<s.length(); i++)
        {
            if (Character.isLowerCase(s.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }
}
