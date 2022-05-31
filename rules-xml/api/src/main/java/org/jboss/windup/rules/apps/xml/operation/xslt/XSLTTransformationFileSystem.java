package org.jboss.windup.rules.apps.xml.operation.xslt;

import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * The next step in building XSLTTransformation {@link Operation} after the absolute path of source xslt file was
 * specified.
 */
public interface XSLTTransformationFileSystem extends XSLTTransformationDescription, OperationBuilder {
    /**
     * Description of the {@link XSLTTransformation} operation
     */
    XSLTTransformationDescription withDescription(String description);
}
