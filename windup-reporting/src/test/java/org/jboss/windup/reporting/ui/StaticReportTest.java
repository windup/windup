package org.jboss.windup.reporting.ui;

import java.io.File;
import java.io.FileWriter;

import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.decoration.Line;
import org.jboss.windup.metadata.decoration.Link;
import org.jboss.windup.metadata.decoration.effort.StoryPointEffort;
import org.jboss.windup.metadata.decoration.effort.UnknownEffort;
import org.jboss.windup.metadata.decoration.hint.MarkdownHint;
import org.jboss.windup.reporting.html.ArchiveReport;
import org.jboss.windup.reporting.html.ResourceReport;
import org.jboss.windup.reporting.html.writer.SummaryHtmlWriter;
import org.junit.Test;


public class StaticReportTest {

	@Test
	public void testWritingStaticReport() throws Exception {
		
		SummaryHtmlWriter writer = new SummaryHtmlWriter();
		
		File file = new File("/tmp/index.html");
		FileWriter overview = new FileWriter(file);

		ArchiveReport archiveReport = new ArchiveReport();
		archiveReport.setEffort(new StoryPointEffort(0));
		archiveReport.setRelativePathFromRoot("test.ear");

		ResourceReport resource = resourceReport();
		archiveReport.getResourceReports().add(resource);
		
		writer.writeStatic(overview, archiveReport);
	}
	
	protected ResourceReport resourceReport() {
		Classification cr = new Classification();
		cr.setDescription("XML File!");
		
		Link lr = new Link();
		lr.setDescription("Red Hat");
		lr.setLink("http://redhat.com");
		lr.setPattern("com.example");
		
		Line lnr = new Line();
		lnr.setDescription("Red Hat");
		lnr.setLineNumber(1);
		MarkdownHint hint = new MarkdownHint();
		hint.setMarkdown("Hello World!!");
		lnr.getHints().add(hint);
		lnr.setPattern("com.example");
		
		String newline = System.getProperty("line.separator");
		MarkdownHint markdownHint = new MarkdownHint();
		markdownHint.setMarkdown("* hello markdown world!"+newline+"* another"+newline+newline+"```java"+newline+"testing"+newline+"```");
		
		lnr.getHints().add(markdownHint);
		
		ResourceReport reportData = new ResourceReport();
		reportData.getDecorations().add(cr);
		reportData.getDecorations().add(lr);
		reportData.getDecorations().add(lnr);
		reportData.setRelativePathFromRootToReport("x");
		reportData.setTitle("x");
		reportData.setSummary("x");
		reportData.setEffort(new UnknownEffort());
		
		boolean[] resource = new boolean[] { true, true, false, true, false };
		reportData.setSourceModification(resource);
		
		return reportData;
	}
}
