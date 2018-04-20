package org.jboss.windup.ast.java;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
class PackageAndClassName
{
    private String packageName;
    private String className;

    public PackageAndClassName(String packageName, String className)
    {
        this.packageName = packageName;
        this.className = className;
    }

    public static PackageAndClassName parseFromQualifiedName(String qualifiedName)
    {
        final String packageName;
        final String className;

        // remove the .* if this was a package import
        if (qualifiedName.contains(".*"))
        {
            packageName = qualifiedName.replace("*", "");
            className = null;
        }
        else
        {
            int lastDot = qualifiedName.lastIndexOf('.');
            if (lastDot == -1)
            {
                packageName = null;
                className = qualifiedName;
            }
            else
            {
                packageName = qualifiedName.substring(0, lastDot);
                className = qualifiedName.substring(lastDot + 1);
            }
        }
        return new PackageAndClassName(packageName, className);
    }

    public String getPackageName()
    {
        return packageName;
    }

    public String getClassName()
    {
        return className;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        if (this.packageName != null)
        {
            sb.append(this.packageName).append(".");
        }
        if (this.className != null)
            sb.append(this.className);

        return sb.toString();
    }
}
