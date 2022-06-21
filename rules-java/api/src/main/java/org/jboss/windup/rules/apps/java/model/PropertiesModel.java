package org.jboss.windup.rules.apps.java.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

import org.jboss.windup.graph.model.TypeValue;

/**
 * Represents a Java-style {@link Properties} file.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(PropertiesModel.TYPE)
public interface PropertiesModel extends FileModel, SourceFileModel {
    public static final String TYPE = "PropertiesModel";

    /**
     * Gets the contents of the file as a {@link Properties} object.
     */
    default Properties getProperties() throws IOException {
        try (InputStream is = this.asInputStream()) {
            Properties props = new Properties();
            props.load(is);
            return props;
        }
    }

}
