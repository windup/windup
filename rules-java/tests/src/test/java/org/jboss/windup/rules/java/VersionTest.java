package org.jboss.windup.rules.java;

import org.jboss.windup.rules.apps.java.condition.Version;
import org.junit.Assert;
import org.junit.Test;

public class VersionTest {
    @Test
    public void versionsCompareTest() {
        Version version = Version.fromVersion("1.2.3").toVersion("3.2.1");
        Assert.assertEquals(true, version.validate("2.2"));
        Assert.assertEquals(true, version.validate("2.2.2.Final"));
        Assert.assertEquals(true, version.validate("1.2.3"));
        Assert.assertEquals(true, version.validate("3.2.1.Final"));
        version = Version.fromVersion("1.0.0.RELEASE").toVersion("2.1.1.RELEASE");
        Assert.assertEquals(true, version.validate("1.2.7.RELEASE"));
        Assert.assertEquals(false, version.validate("2.2.7.RELEASE"));
        Assert.assertEquals(true, version.validate("1.0.0.RELEASE"));
        Assert.assertEquals(true, version.validate("1.0.0.Final"));
        Assert.assertEquals(true, version.validate("2.1.1.RELEASE"));
        Assert.assertEquals(true, version.validate("2.1.1"));
    }
}
