package org.jboss.windup.config.metadata;

import org.jboss.forge.furnace.versions.VersionRange;
import org.jboss.forge.furnace.versions.Versions;

/**
 * Represents a technology with a name (id) and {@link VersionRange}.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class TechnologyReference
{
    private String id;
    private VersionRange versionRange;

    /**
     * Construct a new {@link TechnologyReference} using the given {@link String} ID and {@link String} version range.
     */
    public TechnologyReference(String id, String versionrange)
    {
        this(id, Versions.parseVersionRange(versionrange));
    }

    /**
     * Construct a new {@link TechnologyReference} using the given {@link String} ID and {@link VersionRange}.
     */
    public TechnologyReference(String id, VersionRange versionRange)
    {
        super();
        this.id = id;
        this.versionRange = versionRange;
    }

    /**
     * Get the name/ID of this {@link TechnologyReference}.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Get the {@link VersionRange} of this {@link TechnologyReference}.
     */
    public VersionRange getVersionRange()
    {
        return versionRange;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((versionRange == null) ? 0 : versionRange.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TechnologyReference other = (TechnologyReference) obj;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (versionRange == null)
        {
            if (other.versionRange != null)
                return false;
        }
        else if (!versionRange.equals(other.versionRange))
            return false;
        return true;
    }
}
