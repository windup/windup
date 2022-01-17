package org.jboss.windup.config.metadata;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.versions.MultipleVersionRange;
import org.jboss.forge.furnace.versions.VersionRange;
import org.jboss.forge.furnace.versions.Versions;
import org.jboss.windup.graph.model.TechnologyReferenceModel;

/**
 * Represents a technology with a name (id) and {@link VersionRange}.
 * An example of this would be "eap:7", where "eap" is the id and "7" the version.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class TechnologyReference
{
    private String id;
    private VersionRange versionRange;

    /**
     * DO NOT USE -- This is just here to make proxies possible
     */
    public TechnologyReference()
    {
    }

    public TechnologyReference(TechnologyReferenceModel model)
    {
        this(model.getTechnologyID(), model.getVersionRange());
    }

    /**
     * Construct a new {@link TechnologyReference} using the given {@link String} ID and {@link String} version range.
     */
    public TechnologyReference(String id, String versionRange)
    {
        this.id = id;
        if (versionRange != null)
            this.versionRange = Versions.parseVersionRange(versionRange);
    }

    /**
     * Construct a new {@link TechnologyReference} using the given {@link String} ID and {@link VersionRange}.
     */
    public TechnologyReference(String id, VersionRange versionRange)
    {
        this.id = id;
        this.versionRange = versionRange;
    }

    /**
     * Construct a new {@link TechnologyReference} using the given {@link String} ID.
     */
    public TechnologyReference(String id)
    {
        this(id, (VersionRange) null);
    }

    /**
     * Parses a {@link TechnologyReference} from a string that is formatted as either
     * "id" or "id:versionRange".
     */
    public static TechnologyReference parseFromIDAndVersion(String idAndVersion)
    {
        if (idAndVersion.contains(":"))
        {
            String tech = StringUtils.substringBefore(idAndVersion, ":");
            String versionRangeString = StringUtils.substringAfter(idAndVersion, ":");
            if (!versionRangeString.matches("^[(\\[].*[)\\]]"))
                versionRangeString = "[" + versionRangeString + "]";

            VersionRange versionRange = Versions.parseVersionRange(versionRangeString);
            return new TechnologyReference(tech, versionRange);
        }
        return new TechnologyReference(idAndVersion);
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

    /**
     * Returns true if the other {@link TechnologyReference} has the same technology id and the two version ranges overlap.
     */
    public boolean matches(TechnologyReference other)
    {
        return StringUtils.equals(getId(), other.getId()) && versionRangesOverlap(other.getVersionRange());
    }

    /**
     * Takes the given {@link VersionRange} objects and returns true if there is any overlap between the two
     * ranges.
     *
     * If either is null, then it is treated as overlapping.
     */
    public boolean versionRangesOverlap(VersionRange otherRange)
    {
        if (this.getVersionRange() == null || otherRange == null)
            return true;

        /*
         * FIXME HACK - The code in MultipleVersionRange works pretty well for this calculation, so we are reusing that.
         *
         * Some of the other range intersection algorithms have design flaws that make them return results incorrectly.
         *
         * This hack needs an extensive unit test to insure that it retains the behavior that we expect.
         */
        MultipleVersionRange range1Multiple;
        if (getVersionRange() instanceof MultipleVersionRange)
            range1Multiple = (MultipleVersionRange)getVersionRange();
        else
            range1Multiple = new MultipleVersionRange(getVersionRange());

        try
        {
            VersionRange intersection = range1Multiple.getIntersection(otherRange);
            return intersection != null && !intersection.isEmpty();
        } catch (Throwable t)
        {
            // This generally only occurs if there was no intersection
            return false;
        }
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

    /**
     * This provides a parsable version string based upon the current {@link VersionRange}. If the version
     * range is null, this will return null.
     */
    public String getVersionRangeAsString()
    {
        if (this.versionRange == null)
            return null;

        return this.versionRange.toString();
    }

    @Override
    public String toString()
    {
        String rangeString = getVersionRangeAsString();
        String range = rangeString== null ? "" : ":" + rangeString;
        return id + range;
    }
}
