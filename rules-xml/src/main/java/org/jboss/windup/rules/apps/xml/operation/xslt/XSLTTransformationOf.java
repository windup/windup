package org.jboss.windup.rules.apps.xml.operation.xslt;

import org.jboss.windup.reporting.config.classification.Classification;


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
    
    /**
     * Specify the location of the template xslt file.
     * @param location
     * @return
     */
    public XSLTTransformationFileSystem usingFilesystem(String location)
    {
        transformation.setTemplate(location);
        return transformation;
    }
    
    /**
     * Specify the relative location of the xslt file along with the classloader
     * @param location Location of the xslt file
     * @param classLoader ClassLoader in which the engine should load the location
     * @return
     */
    public  XSLTTransformationLocation using(String location, ClassLoader classLoader)
    {
        // classLoader instance needed to see the file passed in the location
        transformation.setContextClassLoader(classLoader);
        transformation.setTemplate(location);
        return transformation;
    }

    /**
     * Set the text of this {@link Classification}. E.g: "Unparsable XML file." or "Source File"
     */
    public XSLTTransformationLocation using(String location)
    {
        this.transformation.setTemplate(location);
        return this.transformation;
    }
}
