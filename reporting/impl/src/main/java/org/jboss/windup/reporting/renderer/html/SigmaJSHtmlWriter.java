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
import org.jboss.windup.reporting.renderer.gexf.GexfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.tinkerpop.blueprints.Graph;

public class SigmaJSHtmlWriter implements GraphWriter {
	private static final Logger LOG = LoggerFactory.getLogger(SigmaJSHtmlWriter.class);
	
	private final GexfWriter gexfWriter;

	public SigmaJSHtmlWriter(Graph graph) {
		this.gexfWriter = new GexfWriter(graph, "static", "directed", "qualifiedName", "");
	}
	
	public SigmaJSHtmlWriter(Graph graph, String vertexLabelProperty, String edgeLabel) {
		this.gexfWriter = new GexfWriter(graph, "static", "directed", vertexLabelProperty, edgeLabel);
	}

	public void writeGraph(final OutputStream os) throws IOException {
		// read in the html template resource.
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("sigmajs/HtmlTemplate.html");

		String result = null; 
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			gexfWriter.writeGraph(baos);
			result = baos.toString();
		}
		
		if(LOG.isDebugEnabled())  {
			LOG.debug("GEXF: "+result);
		}
		
		// read the document.
		Document document;
		try {
			document = $(is).document();
			// append in the gexf.
			$(document).find("#gexf-source").append(result);

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
