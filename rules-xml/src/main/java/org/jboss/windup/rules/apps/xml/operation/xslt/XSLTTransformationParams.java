package org.jboss.windup.rules.apps.xml.operation.xslt;

import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * Interface specifying there is no more methods to call to build the {@link XSLTTransformation}
 * @author mbriskar
 *
 */
public interface XSLTTransformationParams extends OperationBuilder
{
 // the call of 'withParams()' is the last method of building the XSLTOperation
}
