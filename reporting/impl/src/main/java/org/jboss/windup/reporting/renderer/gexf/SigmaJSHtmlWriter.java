package org.jboss.windup.reporting.renderer.gexf;

import static org.joox.JOOX.$;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.windup.reporting.renderer.GraphWriter;
import java.util.logging.Logger;
import org.jboss.windup.util.Logging;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.tinkerpop.blueprints.Graph;
import java.util.logging.Level;

public class SigmaJSHtmlWriter implements GraphWriter
{
    private static final Logger LOG = Logging.get(SigmaJSHtmlWriter.class);

    private final GexfWriter gexfWriter;

    public SigmaJSHtmlWriter(Graph graph)
    {
        this.gexfWriter = new GexfWriter(graph, "static", "directed", "qualifiedName", "");
    }

    public SigmaJSHtmlWriter(Graph graph, String vertexLabelProperty, String edgeLabel)
    {
        this.gexfWriter = new GexfWriter(graph, "static", "directed", vertexLabelProperty, edgeLabel);
    }

    public void writeGraph(final OutputStream os) throws IOException
    {
        // read in the html template resource.
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("sigmajs/HtmlTemplate.html");

        String result = null;
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            gexfWriter.writeGraph(baos);
            result = baos.toString();
        }

        if (LOG.isLoggable(Level.FINE))
            LOG.fine("GEXF: " + result);

        // read the document.
        Document document;
        try
        {
            document = $(is).document();
            // append in the gexf.
            $(document).find("#gexf-source").append(result);

            writeDocument(document, os);
        }
        catch (SAXException e)
        {
            throw new IOException("Exception loading document.", e);
        }

    }

    public void writeDocument(final Document document, final OutputStream os) throws IOException
    {
        try
        {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(os);
            transformer.transform(source, result);
        }
        catch (TransformerException e)
        {
            throw new IOException("Exception writing to output stream.", e);
        }
    }

}
