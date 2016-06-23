package org.jboss.windup.exec.test;

import org.jboss.forge.furnace.versions.VersionRange;
import org.jboss.forge.furnace.versions.Versions;
import org.jboss.windup.exec.rulefilters.VersionRangeUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class VersionRangeUtilTest
{

    @Test
    public void testVersionRangeIntersectionSimple()
    {
        VersionRange versionRange1 = Versions.parseVersionRange("(10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("(12,17]");

        boolean overlap = VersionRangeUtil.versionRangesOverlap(versionRange1, versionRange2);
        Assert.assertTrue(overlap);
    }

    @Test
    public void testVersionRangeIntersectionSimpleExclusiveBegin()
    {
        VersionRange versionRange1 = Versions.parseVersionRange("[10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("[10,17]");

        boolean overlap = VersionRangeUtil.versionRangesOverlap(versionRange1, versionRange2);
        Assert.assertTrue(overlap);
    }

    @Test
    public void testVersionRangeIntersectionSingleVersionExclusiveBegin()
    {
        VersionRange versionRange1 = Versions.parseVersionRange("[10]");
        VersionRange versionRange2 = Versions.parseVersionRange("(10,17]");

        boolean overlap = VersionRangeUtil.versionRangesOverlap(versionRange1, versionRange2);
        Assert.assertFalse(overlap);
    }

    @Test
    public void testVersionRangeIntersectionSingleVersionInclusiveBegin()
    {
        VersionRange versionRange1 = Versions.parseVersionRange("[10]");
        VersionRange versionRange2 = Versions.parseVersionRange("(10,17]");

        boolean overlap = VersionRangeUtil.versionRangesOverlap(versionRange1, versionRange2);
        Assert.assertFalse(overlap);
    }

    @Test
    public void testVersionRangeIntersectionSimpleNoOverlap()
    {
        VersionRange versionRange1 = Versions.parseVersionRange("(10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("(8,9]");

        boolean overlap = VersionRangeUtil.versionRangesOverlap(versionRange1, versionRange2);
        Assert.assertFalse(overlap);
    }

    @Test
    public void testVersionPartialOverlapAcrossMax()
    {
        VersionRange versionRange1 = Versions.parseVersionRange("(10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("(18,22)");

        boolean overlap = VersionRangeUtil.versionRangesOverlap(versionRange1, versionRange2);
        Assert.assertTrue(overlap);
    }

    @Test
    public void testVersionPartialOverlapAcrossMaxReverse()
    {
        VersionRange versionRange1 = Versions.parseVersionRange("(10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("(18,22)");

        boolean overlap = VersionRangeUtil.versionRangesOverlap(versionRange2, versionRange1);
        Assert.assertTrue(overlap);
    }

    @Test
    public void testVersionPartialOverlapAcrossMin()
    {
        VersionRange versionRange1 = Versions.parseVersionRange("(10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("(7,13)");

        boolean overlap = VersionRangeUtil.versionRangesOverlap(versionRange1, versionRange2);
        Assert.assertTrue(overlap);
    }

    @Test
    public void testVersionPartialOverlapAcrossMinReverse()
    {
        VersionRange versionRange1 = Versions.parseVersionRange("(10,20)");
        VersionRange versionRange2 = Versions.parseVersionRange("(7,13)");

        boolean overlap = VersionRangeUtil.versionRangesOverlap(versionRange2, versionRange1);
        Assert.assertTrue(overlap);
    }
}
