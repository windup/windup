package org.jboss.windup.reporting.ui;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.decoration.Line;
import org.jboss.windup.metadata.decoration.Link;
import org.jboss.windup.metadata.decoration.hint.MarkdownHint;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.reporting.data.ResourceData;
import org.jboss.windup.reporting.data.ResourceDataMarshaller;
import org.junit.Test;

import com.weblogic.TestClass;


public class ReportDataTest {

	@Test
	public void testMarshallingReportDataToXML() throws Exception {
		TestClass testClass = new TestClass();
		
		File reportDirectory = new File("/tmp/test");
		
		File inputFile = new File("/tmp/test/input.txt");
		FileUtils.write(inputFile, "<xml>Example</xml>");
		
		File outputFile = new File("/tmp/test/input.txt.meta");
		
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
		
		ResourceData reportData = new ResourceData();
		reportData.setDecorations(fileMeta.getDecorations());
		
		ResourceDataMarshaller marshaller = new ResourceDataMarshaller();
		marshaller.marshal(outputFile, reportData);
		
	//	FileReader fileReader = new FileReader(outputFile);
	//	ResourceData reportIn = marshaller.unmarshal(fileReader);
	//	System.out.println("Reflected: "+ReflectionToStringBuilder.toString(reportData));
	}
	
	
}
