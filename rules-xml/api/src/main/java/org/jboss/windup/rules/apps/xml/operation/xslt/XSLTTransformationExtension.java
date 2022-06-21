package org.jboss.windup.rules.apps.xml.operation.xslt;

import java.util.Map;

import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * Next step in building the {@link XSLTTransformation}, just after the XSLT extension was selected.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public interface XSLTTransformationExtension extends XSLTTransformationEffort, OperationBuilder {
    /**
     * Specify {@link XSLTTransformation} parameters to be passed to the XSLT template.
     */
    XSLTTransformationEffort withParameters(Map<String, String> parameters);
}
