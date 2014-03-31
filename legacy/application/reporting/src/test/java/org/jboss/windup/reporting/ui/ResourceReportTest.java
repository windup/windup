package org.jboss.windup.reporting.ui;

import java.io.File;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.decoration.Line;
import org.jboss.windup.metadata.decoration.Link;
import org.jboss.windup.metadata.decoration.hint.MarkdownHint;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.reporting.data.ResourceData;
import org.jboss.windup.reporting.html.writer.ResourceHtmlWriter;
import org.jboss.windup.reporting.transformers.FileMetaTransformer;
import org.junit.Test;


public class ResourceReportTest {

	@Test
	public void testResourceReporting() throws Exception {
		File reportDirectory = new File("/tmp/test");
		ResourceHtmlWriter resourceReport = new ResourceHtmlWriter();
		
		File inputFile = new File("/tmp/test/input.txt");
		FileUtils.write(inputFile, "<xml>Example</xml>");
		
		FileMetadata fileMeta = new FileMetadata();
		fileMeta.setFilePointer(inputFile);
		
		Classification cr = new Classification();
		cr.setDescription("XML File!");
		fileMeta.getDecorations().add(cr);
		
		Link lr = new Link();
		lr.setDescription("Red Hat");
		lr.setLink("http://redhat.com");
		fileMeta.getDecorations().add(lr);
		
		Line lnr = new Line();
		lnr.setDescription("Red Hat");
		lnr.setLineNumber(1);
		fileMeta.getDecorations().add(lnr);
		MarkdownHint hint = new MarkdownHint();
		hint.setMarkdown("Hello World!!");
		lnr.getHints().add(hint);
		
		String newline = System.getProperty("line.separator");
		MarkdownHint markdownHint = new MarkdownHint();
		markdownHint.setMarkdown("* hello markdown world!"+newline+"* another"+newline+newline+"```java"+newline+"testing"+newline+"```");
		lnr.getHints().add(markdownHint);

		FileMetaTransformer tf = new FileMetaTransformer();
		ResourceData rd = tf.toResourceData(fileMeta, reportDirectory);
		
		String body = FileUtils.readFileToString(fileMeta.getFilePointer());
		StringWriter stringWriter = new StringWriter();
		resourceReport.writeStatic(stringWriter, body, rd);
		
		System.out.println(stringWriter);
	}
}
