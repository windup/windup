package org.jboss.windup.util;

/**
 * A windup specific class with some useful methods for iterables
 */
public class Iterables
{
    public static int size(Iterable<?> linksToTransformedFiles)
    {
        int resultCount = 0;
        for (Object linksToTransformedFile : linksToTransformedFiles)
        {
            resultCount++;
        }
        return resultCount;
    }
}
