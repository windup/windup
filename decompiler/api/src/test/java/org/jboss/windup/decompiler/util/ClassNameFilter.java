package org.jboss.windup.decompiler.util;

import java.util.zip.ZipEntry;

/**
 * ZipEntry Filter which accepts only one class and its inner classes.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ClassNameFilter implements Filter<ZipEntry>
{

    private final String cls;

    public ClassNameFilter(String cls)
    {
        this.cls = cls.replace('.', '/');
    }

    @Override
    public Result decide(ZipEntry what)
    {
        if (what.isDirectory())
            return Result.REJECT;
        if (!what.getName().startsWith(cls))
            return Result.REJECT;

        final String end = what.getName().substring(cls.length());
        if (end.equals(".class"))
            return Result.ACCEPT;
        if (end.charAt(0) == '$' && end.endsWith(".class"))
            return Result.ACCEPT;

        return Filter.Result.REJECT;
    }

}
