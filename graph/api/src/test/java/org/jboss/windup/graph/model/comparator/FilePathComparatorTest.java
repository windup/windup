package org.jboss.windup.graph.model.comparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class FilePathComparatorTest
{
    @Test
    public void testPathComparator()
    {
        List<String> expectedFilePaths = new ArrayList<>();
        expectedFilePaths.add("/a/foo");
        expectedFilePaths.add("/c/a/");
        expectedFilePaths.add("/c/a.file");
        expectedFilePaths.add("/c/b.file");
        expectedFilePaths.add("/c/c.file");
        expectedFilePaths.add("/c/d.file");
        expectedFilePaths.add("/c/e.file");
        expectedFilePaths.add("/z/bar");
        expectedFilePaths.add("/c/a/anotherfile");
        expectedFilePaths.add("/a/b/c/d/e/f/g/h/i/j/k/l/m/n/o");
        expectedFilePaths.add("/a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p");

        List<String> shuffledList = new ArrayList<>(expectedFilePaths);
        Collections.shuffle(shuffledList);

        Collections.sort(shuffledList, new FilePathComparator());

        Assert.assertEquals(expectedFilePaths.size(), shuffledList.size());
        Assert.assertEquals(expectedFilePaths, shuffledList);
    }

}
