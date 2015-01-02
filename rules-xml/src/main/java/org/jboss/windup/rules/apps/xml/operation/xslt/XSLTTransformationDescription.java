package org.jboss.windup.rules.apps.xml.operation.xslt;

import java.util.Map;

import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * The next step in building {@link XSLTTransformation} just after the description was specified.
 * @author mbriskar
 *
 */
public interface XSLTTransformationDescription extends OperationBuilder
{
    
    /**
     * Extension that is going to be added to the result file.
     * @param extension Examples are .xml,.result etc.
     * @return
     */
    XSLTTransformationExtension withExtension(String extension);
    
    /**
     * Specify {@link XSLTTransformation} parameters. Use this if you don't want to specify extension.
     * @param parameters parameters for the xslt transformer factory
     * @return
     */
    XSLTTransformationParams withParameters(Map<String, String> parameters);
    
}
