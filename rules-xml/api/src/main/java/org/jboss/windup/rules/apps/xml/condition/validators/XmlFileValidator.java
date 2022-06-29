package org.jboss.windup.rules.apps.xml.condition.validators;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * An interface validating a given {@link XmlFileModel} based on the given criteria. It is used with Strategy design pattern.
 */
public interface XmlFileValidator {

    boolean isValid(GraphRewrite event, EvaluationContext context, XmlFileModel model);
}
