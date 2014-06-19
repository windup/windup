package org.jboss.windup.reporting.renderer.html;

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
import org.jboss.windup.reporting.renderer.graphlib.GraphlibWriter;
import org.jboss.windup.reporting.renderer.graphlib.GraphvizConstants.GraphvizDirection;
import org.jboss.windup.reporting.renderer.graphlib.GraphvizConstants.GraphvizType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.tinkerpop.blueprints.Graph;

public class DagreD3JSHtmlWriter implements GraphWriter {
	private static Logger LOG = LoggerFactory.getLogger(DagreD3JSHtmlWriter.class);
	
	private final GraphWriter writer;

	public DagreD3JSHtmlWriter(Graph graph, String vertexLabelProperty, String edgeLabelProperty) {
		this.writer = new GraphlibWriter(graph, GraphvizType.DIGRAPH, GraphvizDirection.TOP_TO_BOTTOM, "g", vertexLabelProperty, edgeLabelProperty);
	}

	public void writeGraph(final OutputStream os) throws IOException {
		// read in the html template resource.
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("dagred3/HtmlTemplate.html");

		String result;
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			writer.writeGraph(baos);
			result = baos.toString();
		}
		
		if(LOG.isDebugEnabled())  {
			LOG.debug("Graphlib: "+result);
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