package org.jboss.windup.reporting.renderer.graphlib;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.jboss.windup.reporting.renderer.GraphWriter;
import org.jboss.windup.reporting.renderer.graphlib.GraphvizConstants.GraphvizDirection;
import org.jboss.windup.reporting.renderer.graphlib.GraphvizConstants.GraphvizType;
import org.jboss.windup.util.Logging;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.joox.JOOX.$;

public class DagreD3JSHtmlWriter implements GraphWriter {
    private static final Logger LOG = Logging.get(DagreD3JSHtmlWriter.class);

    private final GraphlibWriter writer;

    public DagreD3JSHtmlWriter(Graph graph, String vertexLabelProperty, String edgeLabelProperty) {
        this.writer = new GraphlibWriter(graph, GraphvizType.DIGRAPH, GraphvizDirection.TOP_TO_BOTTOM, "g",
                vertexLabelProperty, edgeLabelProperty);
    }

    @Override
    public void writeGraph(Path outputDirectory) throws IOException {
        try (OutputStream os = new FileOutputStream(outputDirectory.resolve("index.html").toFile())) {
            writeGraph(os);
        }
    }

    private void writeGraph(final OutputStream os) throws IOException {
        // read in the html template resource.
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("dagred3/HtmlTemplate.html")) {

            String result;
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                writer.writeGraph(baos);
                result = baos.toString();
            }

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Graphlib: " + result);
            }

            // read the document.
            Document document;
            try {
                document = $(is).document();
                // append in the gexf.
                $(document).find("#graphlib-source").append(result);

                writeDocument(document, os);
            } catch (SAXException e) {
                throw new IOException("Exception loading document.", e);
            }
        }
    }

    public void writeDocument(final Document document, final OutputStream os) throws IOException {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(os);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new IOException("Exception writing to output stream.", e);
        }
    }
}