package org.jboss.windup.rules.apps.xml.operation.xslt;

import java.util.Map;

import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * Next step in building the {@link XSLTTransformation}, just after the XSLT extension was selected.
 * @author mbriskar
 *
 */
public interface XSLTTransformationExtension extends OperationBuilder
{

    /**
     * Specify {@link XSLTTransformation} parameters.
     * @param parameters parameters for the xslt transformer factory
     * @return
     */
    XSLTTransformationParams withParameters(Map<String, String> parameters);
}
