package org.jboss.windup.decompiler.util;

import java.util.zip.ZipEntry;

/**
 * ZipEntry Filter which only accepts certain number of class files, then STOPs.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class CountClassesFilter implements Filter<ZipEntry>
{
    private int curCount = 0;
    private final int maxCount;

    public CountClassesFilter(int maxCount)
    {
        this.maxCount = maxCount;
    }

    @Override
    public Filter.Result decide(ZipEntry what)
    {
        if ((!what.isDirectory()) && what.getName().endsWith(".class"))
            this.curCount++;

        if (this.curCount >= this.maxCount)
            return Filter.Result.STOP;

        return Filter.Result.ACCEPT;
    }

}
