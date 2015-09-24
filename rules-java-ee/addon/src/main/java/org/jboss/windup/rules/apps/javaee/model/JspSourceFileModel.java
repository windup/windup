package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a JSP file on disk.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(JspSourceFileModel.TYPE)
public interface JspSourceFileModel extends AbstractJavaSourceModel
{
    String UNPARSEABLE_JSP_CLASSIFICATION = "Unparseable JSP File";
    String UNPARSEABLE_JSP_DESCRIPTION = "This JSP file could not be parsed";

    String TYPE = "JspSourceFile";
}
