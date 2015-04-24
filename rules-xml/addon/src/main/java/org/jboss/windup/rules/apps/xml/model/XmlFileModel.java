package org.jboss.windup.rules.apps.xml.model;

import java.io.InputStream;
import java.util.logging.Logger;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.xml.LocationAwareXmlReader;
import org.w3c.dom.Document;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(XmlFileModel.TYPE)
public interface XmlFileModel extends FileModel, SourceFileModel
{
    public static final Logger LOG = Logger.getLogger(XmlFileModel.class.getName());

    public static final String UNPARSEABLE_XML_CLASSIFICATION = "Unparseable XML File";
    public static final String UNPARSEABLE_XML_DESCRIPTION = "This file could not be parsed";

    public static final String ROOT_TAG_NAME = "rootTagName";
    public static final String NAMESPACE = "namespace";
    public static final String DOCTYPE = "doctype";
    public static final String TYPE = "XmlFileModel";

    @Adjacency(label = DOCTYPE, direction = Direction.OUT)
    public void setDoctype(DoctypeMetaModel doctype);

    @Adjacency(label = DOCTYPE, direction = Direction.OUT)
    public DoctypeMetaModel getDoctype();

    @Adjacency(label = NAMESPACE, direction = Direction.OUT)
    public void addNamespace(NamespaceMetaModel namespace);

    @Adjacency(label = NAMESPACE, direction = Direction.OUT)
    public Iterable<NamespaceMetaModel> getNamespaces();

    @Indexed
    @Property(ROOT_TAG_NAME)
    public String getRootTagName();

    @Property(ROOT_TAG_NAME)
    public void setRootTagName(String rootTagName);

    @JavaHandler
    public Document asDocument();

    abstract class Impl implements XmlFileModel, JavaHandlerContext<Vertex>
    {
        @Override
        public Document asDocument()
        {
            Document document = XMLDocumentCache.get(this);
            if (document == null)
            {
                FileModel fileModel = frame(asVertex(), FileModel.class);
                try (InputStream is = fileModel.asInputStream())
                {
                    document = LocationAwareXmlReader.readXML(is);
                    XMLDocumentCache.put(this, document);
                }
                catch (Exception e)
                {
                    throw new WindupException("Exception reading document.", e);
                }
            }

            return document;
        }

    }
}
