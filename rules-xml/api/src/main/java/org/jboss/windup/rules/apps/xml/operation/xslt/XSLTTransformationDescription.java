package org.jboss.windup.rules.apps.xml.operation.xslt;

import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * The next step in building {@link XSLTTransformation} just after the description was specified.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public interface XSLTTransformationDescription extends XSLTTransformationExtension, OperationBuilder {
    /**
     * Extension to be appended to the result file.
     */
    XSLTTransformationExtension withExtension(String extension);
}
