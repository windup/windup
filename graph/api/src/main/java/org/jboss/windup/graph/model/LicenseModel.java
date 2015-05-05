package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(LicenseModel.TYPE)
public interface LicenseModel extends FileModel, SourceFileModel {

	public static final String TYPE = "LicenseModel";
	public static final String NAME = "licenseName";
	public static final String URL = "licenseURL";
	
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
    @Property(NAME)
    void setURL(String url);

}
