package org.jboss.windup.rules.apps.xml.operation.xslt;

import java.util.Map;

import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * The next step in building XSLTTransformation {@link Operation} after the absolute path of source xslt file was specified.
 *
 */
public interface XSLTTransformationFileSystem extends OperationBuilder
{

    /**
     * Description of the {@link XSLTTransformation} operation
     * @param description
     * @return
     */
    XSLTTransformationDescription withDescription(String description);
    
    /**
     * Extension that is going to be added to the result file. Use this if you don't want to specify the description.
     * @param extension Examples are .xml,.result etc.
     * @return
     */
    XSLTTransformationExtension withExtension(String extension);
    
    /**
     * Specify {@link XSLTTransformation} parameters. Use this if you don't want to specify description nor extension.
     * @param parameters parameters for the xslt transformer factory
     * @return
     */
    XSLTTransformationParams withParameters(Map<String, String> parameters);
}
