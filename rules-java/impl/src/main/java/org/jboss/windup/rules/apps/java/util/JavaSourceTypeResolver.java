package org.jboss.windup.rules.apps.java.util;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.SourceTypeResolver;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;

/**
 * Resolves java-related sources to their type for reporting purposes.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class JavaSourceTypeResolver implements SourceTypeResolver
{
    @Override
    public String resolveSourceType(FileModel f)
    {
        String filename = f.getFileName();
        if (filename.endsWith(".properties"))
        {
            return "properties";
        }
        else if (filename.equalsIgnoreCase("MANIFEST.MF"))
        {
            return "manifest";
        }
        else if (f instanceof JarManifestModel)
        {
            return "manifest";
        }
        else if (f instanceof JavaSourceFileModel)
        {
            return "java";
        }
        else
        {
            return null;
        }
    }
}
