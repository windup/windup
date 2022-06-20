package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;


/**
 * Represents a JSF template (either in jsp syntax or jsf syntax).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(JsfSourceFileModel.TYPE)
public interface JsfSourceFileModel extends AbstractJavaSourceModel {
    String TYPE = "JsfSourceFile";


}
