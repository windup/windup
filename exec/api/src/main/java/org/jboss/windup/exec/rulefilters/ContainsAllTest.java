package org.jboss.windup.exec.rulefilters;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

public class ContainsAllTest
{

    @Test
    public void test()
    {
        Set<String> haystack = new HashSet<>();
        haystack.add("foo");
        haystack.add("bar");

        Assert.assertTrue(haystack.containsAll(new HashSet<>()));
        Assert.assertTrue(CollectionUtils.containsAny(haystack, new HashSet<>()));
    }

}
