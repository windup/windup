package org.jboss.windup.reporting;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.SourceTypeResolver;

public class DefaultSourceTypeResolver implements SourceTypeResolver
{

    @Override
    public String resolveSourceType(FileModel f)
    {
        String filename = f.getFileName();
        if (filename == null)
        {
            return null;
        }
        else if (filename.endsWith(".java"))
        {
            return "java";
        }
        else if (filename.endsWith(".xml"))
        {
            return "xml";
        }
        else if (filename.endsWith(".properties"))
        {
            return "properties";
        }
        else if (filename.equalsIgnoreCase("MANIFEST.MF"))
        {
            return "manifest";
        }
        else
        {
            return null;
        }
    }

}
