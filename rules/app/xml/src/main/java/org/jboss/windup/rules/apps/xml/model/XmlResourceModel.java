package org.jboss.windup.rules.apps.xml.model;

import java.io.InputStream;

import org.jboss.windup.graph.model.resource.FileModel;
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

@TypeValue("XmlResource")
public interface XmlResourceModel extends FileModel
{
    @Adjacency(label = "doctype", direction = Direction.OUT)
    public void setDoctype(DoctypeMetaModel doctype);

    @Adjacency(label = "doctype", direction = Direction.OUT)
    public DoctypeMetaModel getDoctype();

    @Adjacency(label = "namespace", direction = Direction.OUT)
    public void addNamespace(NamespaceMetaModel namespace);

    @Adjacency(label = "namespace", direction = Direction.OUT)
    public Iterable<NamespaceMetaModel> getNamespaces();

    @Property("rootTagName")
    public String getRootTagName();

    @Property("rootTagName")
    public void setRootTagName(String rootTagName);

    @JavaHandler
    public Document asDocument();

    abstract class Impl implements XmlResourceModel, JavaHandlerContext<Vertex>
    {

        @Override
        public Document asDocument()
        {
            FileModel fileModel = frame(asVertex(), FileModel.class);
            try (InputStream is = fileModel.asInputStream())
            {
                Document parsedDocument = LocationAwareXmlReader.readXML(is);
                return parsedDocument;
            }
            catch (Exception e)
            {
                throw new WindupException("Exception reading document.", e);
            }
        }

    }
}
