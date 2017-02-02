package org.jboss.windup.tooling.rules;

import org.apache.commons.lang.StringUtils;

public class TechnologyImpl implements Technology 
{
    private static final long serialVersionUID = 1L;

    public static final String TECHNOLOGY_ID = "technology_id";

    private Long id;

    private int version;

    private String name;
    private String versionRange;

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public void setId(Long id)
    {
        this.id = id;
    }

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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof Technology))
            return false;

        Technology that = (Technology) o;

        return id != null ? id.equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        String versionRangeSuffix = StringUtils.isNotBlank(this.versionRange) ? ":" + this.versionRange : "";

        return this.name + versionRangeSuffix;
    }
}
