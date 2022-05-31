package org.jboss.windup.rules.apps.xml.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.xml.LocationAwareXmlReader;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

@TypeValue(XmlFileModel.TYPE)
public interface XmlFileModel extends FileModel, SourceFileModel {
    Logger LOG = Logger.getLogger(XmlFileModel.class.getName());

    String NOT_VALID_XML = "XML File is not valid";
    String XSD_URL_NOT_VALID = "XML File references not valid xsd url.";


    String ROOT_TAG_NAME = "rootTagName";
    String NAMESPACE = "namespace";
    String DOCTYPE = "doctype";
    String TYPE = "XmlFileModel";

    @Adjacency(label = DOCTYPE, direction = Direction.OUT)
    DoctypeMetaModel getDoctype();

    @Adjacency(label = DOCTYPE, direction = Direction.OUT)
    void setDoctype(DoctypeMetaModel doctype);

    @Adjacency(label = NAMESPACE, direction = Direction.OUT)
    void addNamespace(NamespaceMetaModel namespace);

    @Adjacency(label = NAMESPACE, direction = Direction.OUT)
    List<NamespaceMetaModel> getNamespaces();

    @Indexed
    @Property(ROOT_TAG_NAME)
    String getRootTagName();

    @Property(ROOT_TAG_NAME)
    void setRootTagName(String rootTagName);

    default Document asDocument() {
        XMLDocumentCache.Result cacheResult = XMLDocumentCache.get(this);
        Document document;
        if (cacheResult.isParseFailure()) {
            throw new WindupException("Could not load " + asFile() + " due to previous parse failure");
        } else if (cacheResult.getDocument() == null) {
            FileModel fileModel = getGraph().frameElement(getElement(), FileModel.class);
            try (InputStream is = fileModel.asInputStream()) {
                document = LocationAwareXmlReader.readXML(is);
                XMLDocumentCache.cache(this, document);
            } catch (Exception e) {
                XMLDocumentCache.cacheParseFailure(this);
                throw new WindupException("Exception reading document due to: " + e.getMessage(), e);
            }
        } else {
            document = cacheResult.getDocument();
        }

        return document;
    }
}
