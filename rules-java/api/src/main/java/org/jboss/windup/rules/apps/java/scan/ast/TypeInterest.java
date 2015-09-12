package org.jboss.windup.rules.apps.java.scan.ast;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TypeInterest
{
    private String packagePrefix;
    private String className;
    private String methodName;

    public TypeInterest(String packagePrefix)
    {
        this.packagePrefix = packagePrefix;
    }

    public TypeInterest(String packagePrefix, String className)
    {
        this.packagePrefix = packagePrefix;
        this.className = className;
    }

    public TypeInterest(String packagePrefix, String className, String methodName)
    {
        this.packagePrefix = packagePrefix;
        this.className = className;
        this.methodName = methodName;
    }

    public String getPackagePrefix()
    {
        return packagePrefix;
    }

    public String getClassName()
    {
        return className;
    }

    public String getMethodName()
    {
        return methodName;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TypeInterest that = (TypeInterest) o;

        if (packagePrefix != null ? !packagePrefix.equals(that.packagePrefix) : that.packagePrefix != null)
            return false;
        if (className != null ? !className.equals(that.className) : that.className != null)
            return false;
        return !(methodName != null ? !methodName.equals(that.methodName) : that.methodName != null);

    }

    @Override
    public int hashCode()
    {
        int result = packagePrefix != null ? packagePrefix.hashCode() : 0;
        result = 31 * result + (className != null ? className.hashCode() : 0);
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "TypeInterest{" +
                    "packagePrefix='" + packagePrefix + '\'' +
                    ", className='" + className + '\'' +
                    ", methodName='" + methodName + '\'' +
                    '}';
    }
}
