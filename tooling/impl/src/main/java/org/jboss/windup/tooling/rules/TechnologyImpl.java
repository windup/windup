package org.jboss.windup.tooling.rules;

public class TechnologyImpl implements Technology
{
    public static final String TECHNOLOGY_ID = "technology_id";
    private static final long serialVersionUID = 1L;
    private int version;

    private String name;
    private String versionRange;

    @Override
    public int getVersion()
    {
        return version;
    }

    @Override
    public void setVersion(int version)
    {
        this.version = version;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getVersionRange()
    {
        return versionRange;
    }

    @Override
    public void setVersionRange(String versionRange)
    {
        this.versionRange = versionRange;
    }
}
