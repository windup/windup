package org.jboss.windup.rules.apps.xml;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.SourceTypeResolver;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

/**
 * Resolves xml-related sources to their type for reporting purposes.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class XmlSourceTypeResolver implements SourceTypeResolver
{
    @Override
    public String resolveSourceType(FileModel f)
    {
        if (f instanceof XmlFileModel)
        {
            return "xml";
        }
        else
        {
            return null;
        }
    }
}
