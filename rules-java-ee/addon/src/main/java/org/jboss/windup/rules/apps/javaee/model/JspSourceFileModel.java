package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;

/**
 * Represents a JSP file on disk.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(JspSourceFileModel.TYPE)
public interface JspSourceFileModel extends AbstractJavaSourceModel {
    String TYPE = "JspSourceFileModel";
}
