package org.jboss.windup.rules.apps.xml.operation.xslt;

import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * Next step in building {@link XSLTTransformation} operation
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public interface XSLTTransformationLocation extends XSLTTransformationDescription, OperationBuilder {
    /**
     * Description of the {@link XSLTTransformation} operation
     */
    XSLTTransformationDescription withDescription(String description);
}
