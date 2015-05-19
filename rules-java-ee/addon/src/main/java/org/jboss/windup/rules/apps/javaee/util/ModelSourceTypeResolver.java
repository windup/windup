package org.jboss.windup.rules.apps.javaee.util;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.SourceTypeResolver;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

/**
 * Resolves the Model types to the appropriate syntax highlighter.
 *  
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 *
 */
public class ModelSourceTypeResolver implements SourceTypeResolver
{

    @Override
    public String resolveSourceType(FileModel f)
    {
        if(f instanceof XmlFileModel) {
            return "xml";
        }
        else if(f instanceof JarManifestModel) {
            return "manifest";
        }
        else if(f instanceof JavaSourceFileModel) {
            return "java";
        }
        
        return null;
    }

}
