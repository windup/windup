package org.jboss.windup.rules.java;

import org.jboss.windup.rules.apps.java.condition.Version;
import org.junit.Assert;
import org.junit.Test;

public class VersionTest {
    @Test
    public void versionsCompareTest() {
        Version version = Version.fromVersion("1.2.3").to("3.2.1");
        Assert.assertTrue(version.validate("2.2"));
        Assert.assertTrue(version.validate("2.2.2.Final"));
        Assert.assertTrue(version.validate("1.2.3"));
        Assert.assertTrue(version.validate("3.2.1.Final"));
        version = Version.fromVersion("1.0.0.RELEASE").to("2.1.1.RELEASE");
        Assert.assertTrue(version.validate("1.2.7.RELEASE"));
        Assert.assertFalse(version.validate("2.2.7.RELEASE"));
        Assert.assertTrue(version.validate("1.0.0.RELEASE"));
        Assert.assertTrue(version.validate("1.0.0.Final"));
        Assert.assertTrue(version.validate("2.1.1.RELEASE"));
        Assert.assertTrue(version.validate("2.1.1"));
    }
}
