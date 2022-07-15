package org.jboss.windup.rules.apps.xml.operation.xslt;

import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * Next step in building {@link XSLTTransformation} operation
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface XSLTTransformationOf extends XSLTTransformationEffort, OperationBuilder {
    /**
     * Set the location of the XSLT template to be used.
     */
    XSLTTransformationLocation usingTemplate(String location);

    /**
     * Set the location of the XSLT template to be used, and the {@link ClassLoader} within which the template is
     * contained.
     */
    XSLTTransformationLocation usingTemplate(String location, ClassLoader loader);
}
