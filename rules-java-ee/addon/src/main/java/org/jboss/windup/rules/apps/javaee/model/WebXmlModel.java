package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

import java.util.List;

/**
 * Represents the data from a Java EE web.xml file.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(WebXmlModel.TYPE)
public interface WebXmlModel extends XmlFileModel {
    String TYPE = "WebXmlModel";
    String DISPLAY_NAME = "displayName";
    String SPECIFICATION_VERSION = "specificationVersion";
    String WEB_XML_TO_ENVIRONMENT_REFERENCE = "webXmlToEnvironmentReference";
    String WEB_XML_TO_RESOURCE_REFERENCE = "webXmlToResourceReference";

    /**
     * Gets the EE Specification version specified by this web.xml file
     */
    @Property(SPECIFICATION_VERSION)
    String getSpecificationVersion();

    /**
     * Sets the EE Specification version specified by this web.xml file
     */
    @Property(SPECIFICATION_VERSION)
    void setSpecificationVersion(String version);

    /**
     * Gets the web.xml display-name property
     */
    @Property(DISPLAY_NAME)
    String getDisplayName();

    /**
     * Gets the web.xml display-name property
     */
    @Property(DISPLAY_NAME)
    void setDisplayName(String displayName);


    /**
     * Maintains a list of {@link EnvironmentReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = WEB_XML_TO_ENVIRONMENT_REFERENCE, direction = Direction.OUT)
    List<EnvironmentReferenceModel> getEnvironmentReferences();

    /**
     * Maintains a list of {@link EnvironmentReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = WEB_XML_TO_ENVIRONMENT_REFERENCE, direction = Direction.OUT)
    void addEnvironmentReference(EnvironmentReferenceModel environmentReference);

    /**
     * Maintains a list of {@link WebXmlResourceReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = WEB_XML_TO_RESOURCE_REFERENCE, direction = Direction.OUT)
    List<WebXmlResourceReferenceModel> getResourceReferences();

    /**
     * Maintains a list of {@link WebXmlResourceReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = WEB_XML_TO_RESOURCE_REFERENCE, direction = Direction.OUT)
    void addEnvironmentReference(WebXmlResourceReferenceModel environmentReference);

}
