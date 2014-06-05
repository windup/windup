package org.jboss.windup.exec.visitor.reporter.html.model;

import java.io.File;

import org.apache.commons.lang.StringUtils;

public class ReportContext
{
    private final String relativeTo;
    private final String relativeFrom;

    public ReportContext(File base, File report)
    {
        if (base.isFile())
        {
            base = base.getParentFile();
        }

        String relativeTo = base.toURI().relativize(report.toURI()).getPath();
        int count = StringUtils.countMatches(relativeTo, "/");
        StringBuilder relativeFromBuilder = new StringBuilder();
        for (int i = 0; i < count; i++)
        {
            if (i > 0)
            {
                relativeFromBuilder.append("/");
            }
            relativeFromBuilder.append("..");
        }
        // always add a trailing slash
        if (count > 0)
        {
            relativeFromBuilder.append("/");
        }

        if (StringUtils.equals(relativeTo, "/"))
        {
            relativeTo = "";
        }

        this.relativeTo = relativeTo;
        this.relativeFrom = relativeFromBuilder.toString();
    }

    public String getRelativeFrom()
    {
        return relativeFrom;
    }

    public String getRelativeTo()
    {
        return relativeTo;
    }
}
