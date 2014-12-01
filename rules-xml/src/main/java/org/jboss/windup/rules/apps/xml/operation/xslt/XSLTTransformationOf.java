package org.jboss.windup.rules.apps.xml.operation.xslt;

import org.jboss.windup.reporting.config.Classification;


/**
 * Intermediate step for constructing {@link XSLTTransformation} instances for a specified ref.
 * 
 * @author mbriskar
 */
public class XSLTTransformationOf
{
    private XSLTTransformation transformation;

    XSLTTransformationOf(String variable)
    {
        this.transformation = new XSLTTransformation(variable);
    }
    
    public XSLTTransformation usingFilesystem(String location)
    {
        transformation.setSourceLocation(location);
        return transformation;
    }
    
    public  XSLTTransformation using(String location, ClassLoader classLoader)
    {
        // classLoader instance needed to see the file passed in the location
        transformation.setContextClassLoader(classLoader);
        transformation.setSourceLocation(location);
        return transformation;
    }

    /**
     * Set the text of this {@link Classification}. E.g: "Unparsable XML file." or "Source File"
     */
    public XSLTTransformation using(String location)
    {
        this.transformation.setSourceLocation(location);
        return this.transformation;
    }
}
