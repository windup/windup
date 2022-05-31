package org.jboss.windup.rules.apps.xml.condition.validators;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.HashSet;
import java.util.Set;

/**
 * A validator used to disallow to query against the same xmlfile twice within one {@link XmlFile.evaluate()} call.
 * This may happen in the case of connecting multiple XmlFile conditions from which the first one
 * returns multiple places in an XmlFile.
 */
public class XmlCacheValidator implements XmlFileValidator {

    Set<String> xmlCache = new HashSet<>();

    @Override
    public boolean isValid(GraphRewrite event, EvaluationContext context, XmlFileModel model) {
        if (xmlCache.contains(model.getFilePath())) {
            return false;
        } else {
            xmlCache.add(model.getFilePath());
        }
        return true;
    }

    public void clear() {
        this.xmlCache.clear();
    }
}
