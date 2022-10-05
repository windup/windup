package org.jboss.windup.config.metadata;

import org.jboss.forge.furnace.versions.VersionRange;
import org.jboss.forge.furnace.versions.Versions;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TechnologyReferenceTest {

    @Test
    public void testVersionRangeIntersectionSimple() {
        VersionRange versionRange1 = Versions.parseVersionRange("(10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("(12,17]");
        TechnologyReference technologyReference = new TechnologyReference("tech", versionRange1);

        boolean overlap = technologyReference.versionRangesOverlap(versionRange2);
        Assert.assertTrue(overlap);
    }

    @Test
    public void testVersionRangeIntersectionSimpleExclusiveBegin() {
        VersionRange versionRange1 = Versions.parseVersionRange("[10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("[10,17]");
        TechnologyReference technologyReference = new TechnologyReference("tech", versionRange1);

        boolean overlap = technologyReference.versionRangesOverlap(versionRange2);
        Assert.assertTrue(overlap);
    }

    @Test
    public void testVersionRangeIntersectionSingleVersionExclusiveBegin() {
        VersionRange versionRange1 = Versions.parseVersionRange("[10]");
        VersionRange versionRange2 = Versions.parseVersionRange("(10,17]");
        TechnologyReference technologyReference = new TechnologyReference("tech", versionRange1);

        boolean overlap = technologyReference.versionRangesOverlap(versionRange2);
        Assert.assertFalse(overlap);
    }

    @Test
    public void testVersionRangeIntersectionSingleVersionInclusiveBegin() {
        VersionRange versionRange1 = Versions.parseVersionRange("[10]");
        VersionRange versionRange2 = Versions.parseVersionRange("[10,17]");
        TechnologyReference technologyReference = new TechnologyReference("tech", versionRange1);

        boolean overlap = technologyReference.versionRangesOverlap(versionRange2);
        Assert.assertTrue(overlap);
    }

    @Test
    public void testVersionRangeIntersectionSimpleNoOverlap() {
        VersionRange versionRange1 = Versions.parseVersionRange("(10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("(8,9]");
        TechnologyReference technologyReference = new TechnologyReference("tech", versionRange1);

        boolean overlap = technologyReference.versionRangesOverlap(versionRange2);
        Assert.assertFalse(overlap);
    }

    @Test
    public void testVersionPartialOverlapAcrossMax() {
        VersionRange versionRange1 = Versions.parseVersionRange("(10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("(18,22)");
        TechnologyReference technologyReference = new TechnologyReference("tech", versionRange1);

        boolean overlap = technologyReference.versionRangesOverlap(versionRange2);
        Assert.assertTrue(overlap);
    }

    @Test
    public void testVersionPartialOverlapAcrossMaxReverse() {
        VersionRange versionRange1 = Versions.parseVersionRange("(10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("(18,22)");
        TechnologyReference technologyReference = new TechnologyReference("tech", versionRange2);

        boolean overlap = technologyReference.versionRangesOverlap(versionRange1);
        Assert.assertTrue(overlap);
    }

    @Test
    public void testVersionPartialOverlapAcrossMin() {
        VersionRange versionRange1 = Versions.parseVersionRange("(10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("(7,13)");
        TechnologyReference technologyReference = new TechnologyReference("tech", versionRange1);

        boolean overlap = technologyReference.versionRangesOverlap(versionRange2);
        Assert.assertTrue(overlap);
    }

    @Test
    public void testVersionPartialOverlapAcrossMinReverse() {
        VersionRange versionRange1 = Versions.parseVersionRange("(10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("(7,13)");
        TechnologyReference technologyReference = new TechnologyReference("tech", versionRange1);

        boolean overlap = technologyReference.versionRangesOverlap(versionRange2);
        Assert.assertTrue(overlap);
    }
}
