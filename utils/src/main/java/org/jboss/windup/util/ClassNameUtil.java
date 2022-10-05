package org.jboss.windup.util;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ClassNameUtil {
    public static final String getPackageName(String qualifiedName) {
        String packageName = "";
        if (qualifiedName.contains("."))
            packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
        return packageName;
    }
}
