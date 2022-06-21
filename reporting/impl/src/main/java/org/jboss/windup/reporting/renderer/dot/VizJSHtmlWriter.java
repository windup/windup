package org.jboss.windup.reporting.renderer.dot;

import static org.joox.JOOX.$;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.jboss.windup.reporting.renderer.GraphWriter;
import org.jboss.windup.reporting.renderer.dot.DotConstants.DotGraphType;
import org.jboss.windup.util.Logging;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class VizJSHtmlWriter implements GraphWriter {
    private static Logger LOG = Logging.get(VizJSHtmlWriter.class);

    private final DotWriter writer;

    public VizJSHtmlWriter(Graph graph) {
        this.writer = new DotWriter(graph, "G", "qualifiedName", "", DotGraphType.DIGRAPH, "8pt");
    }

    public VizJSHtmlWriter(Graph graph, String vertexLabelProperty, String edgeLabel) {
        this.writer = new DotWriter(graph, "G", vertexLabelProperty, edgeLabel, DotGraphType.DIGRAPH, "8pt");
    }

    public void writeGraph(final Path outputDirectory) throws IOException {
        // copy vis.js to the output folder
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("vizjs/viz.js")) {
            try (OutputStream outputStream = new FileOutputStream(outputDirectory.resolve("viz.js").toFile())) {
                IOUtils.copy(is, outputStream);
            }
        }

        // read in the html template resource.
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("vizjs/HtmlTemplate.html")) {
            Path indexHTML = outputDirectory.resolve("index.html");
            try (OutputStream os = new FileOutputStream(indexHTML.toFile())) {
                String result;
                {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    writer.writeGraph(baos);
                    result = baos.toString();
                }

                if (LOG.isLoggable(Level.FINE))
                    LOG.fine("DOT: " + result);

                // read the document.
                Document document;
                try {
                    document = $(is).document();
                    // append in the gexf.
                    $(document).find("#dot-source").append(result);

                    writeDocument(document, os);
                } catch (SAXException e) {
                    throw new IOException("Exception loading document.", e);
                }
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