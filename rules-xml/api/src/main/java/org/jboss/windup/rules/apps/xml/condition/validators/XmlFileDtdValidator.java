package org.jboss.windup.rules.apps.xml.condition.validators;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.rules.apps.xml.model.DoctypeMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * A validator used to query against dtd attributes such as publicId and systemId
 */
public class XmlFileDtdValidator implements XmlFileValidator {
    private String publicId;
    private String systemId;

    @Override
    public boolean isValid(GraphRewrite event, EvaluationContext context, XmlFileModel model) {
        if ((publicId != null && !publicId.isEmpty()) || systemId != null) {
            DoctypeMetaModel doctype = model.getDoctype();
            if (doctype == null) {
                return false;
            }
            if (publicId != null && ((doctype.getPublicId() == null) || !doctype.getPublicId().matches(publicId))) {
                return false;
            }
            if (systemId != null && ((doctype.getSystemId() == null) || !doctype.getSystemId().matches(systemId))) {
                return false;
            }

        }
        return true;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }
}
