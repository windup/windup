package org.jboss.windup.rules.apps.xml.operation.xslt;

import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * Final optional step in building the {@link XSLTTransformation}, just after the XSLT extension was selected.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public interface XSLTTransformationEffort extends OperationBuilder {
    /**
     * Specify {@link XSLTTransformation} estimated effort.
     */
    OperationBuilder withEffort(int effort);
}
