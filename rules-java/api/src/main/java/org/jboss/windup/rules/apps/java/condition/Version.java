package org.jboss.windup.rules.apps.java.condition;

import org.apache.maven.artifact.versioning.ComparableVersion;

/**
 * Object used to specify the version range
 */
public class Version
{
    private String from;
    private String to;

    public static Version fromVersion(String from)
    {
        Version v = new Version();
        v.setFrom(from);
        return v;
    }

    public static Version toVersion(String to)
    {
        Version v = new Version();
        v.setTo(to);
        return v;
    }

    public Version to(String to)
    {
        this.setTo(to);
        return this;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public boolean validate(String versionString)
    {
        boolean result = true;
        if (from != null)
        {
            result = firstVersionLesser(from, versionString);
        }
        if (result && to != null)
        {
            result = firstVersionLesser(versionString, to);
        }
        return result;
    }

    private boolean firstVersionLesser(String first, String second)
    {
        ComparableVersion firstVersion  = new ComparableVersion(first);
        ComparableVersion secondVersion = new ComparableVersion(second);

        return firstVersion.compareTo(secondVersion) <= 0;
    }

    public String toString()
    {
        return "Version (" + from + ", " + to + ")";
    }

}
