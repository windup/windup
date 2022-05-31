package org.jboss.windup.graph.model;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

@TypeValue(LicenseModel.TYPE)
public interface LicenseModel extends FileModel, SourceFileModel {
    String TYPE = "LicenseModel";
    String NAME = "licenseName";
    String URL = "licenseURL";

    /**
     * Indicates the license name.
     */
    @Property(NAME)
    String getName();

    /**
     * Indicates the license name.
     */
    @Property(NAME)
    void setName(String name);

    /**
     * Indicates the license url.
     */
    @Property(URL)
    String getURL();

    /**
     * Indicates the license url.
     */
    @Property(URL)
    void setURL(String url);

}
