/*
 * Copyright (c) 2013 Red Hat, Inc. and/or its affiliates.
 *  
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *      Brad Davis - bradsdavis@gmail.com - Initial API and implementation
*/
package org.jboss.windup.reporting.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.ReportUtil;
import org.jboss.windup.reporting.Reporter;
import org.jboss.windup.reporting.html.writer.ClassloaderHtmlWriter;
import org.jboss.windup.reporting.html.writer.ResourceHtmlWriter;
import org.jboss.windup.reporting.html.writer.SummaryHtmlWriter;
import org.jboss.windup.reporting.transformers.ArchiveMetaTransformer;
import org.jboss.windup.reporting.transformers.MetaResultTransformResolver;
import org.jboss.windup.reporting.transformers.MetaResultTransformer;


public class StaticHtmlReporter implements Reporter {
	private static final Log LOG = LogFactory.getLog(StaticHtmlReporter.class);
	private MetaResultTransformResolver resolver;
	
	public void setResolver(MetaResultTransformResolver resolver) {
		this.resolver = resolver;
	}
	
	@Override
	public void process(ArchiveMetadata archive, File reportDirectory) {
		//create all reports...
		ArchiveReport archiveReport = this.toArchiveReport(archive, reportDirectory);
		writeStaticResourceReports(archiveReport, reportDirectory);
		writeSupportingFiles(reportDirectory);
		writeStaticSummaryReport(archiveReport, reportDirectory);
	}
	
	protected void writeSupportingFiles(File reportDirectory) {
		try {
			writeSupportedFile("snippet/jquery.snippet.min.js", reportDirectory);
			writeSupportedFile("snippet/jquery.snippet.min.css", reportDirectory);
			writeSupportedFile("snippet/jquery.snippet.java-manifest.js", reportDirectory);
			writeSupportedFile("jquery.min.js", reportDirectory);
			writeSupportedFile("windup.css", reportDirectory);
			writeSupportedFile("windup.js", reportDirectory);
			writeSupportedFile("img/windup-logo.png", reportDirectory);
			writeSupportedFile("img/rh-logo.png", reportDirectory);
			writeSupportedFile("flot/jquery.flot.min.js", reportDirectory);
			writeSupportedFile("flot/jquery.flot.pie.min.js", reportDirectory);
			writeSupportedFile("jquery-ui/jquery.ui.widget.js", reportDirectory);
			writeSupportedFile("sausage/jquery.sausage.min.js", reportDirectory);
			writeSupportedFile("sausage/sausage.css", reportDirectory);
			writeSupportedFile("img/styles/glyphicons_195_circle_info.png", reportDirectory);
			writeSupportedFile("img/styles/glyphicons_196_circle_exclamation_mark.png", reportDirectory);
			writeSupportedFile("img/styles/glyphicons_217_circle_arrow_right.png", reportDirectory);
			writeSupportedFile("img/favicon.png", reportDirectory);
	
			writeSupportedFile("jquery-collapse/jquery.collapse.js", reportDirectory);
			writeSupportedFile("jquery-collapse/jquery.collapse_storage.js", reportDirectory);
			writeSupportedFile("jquery-collapse/jquery.collapse_cookie_storage.js", reportDirectory);
		}
		catch(IOException e) {
			LOG.error("Exception writing supporting file.", e);
		}
	}
	
	protected void writeSupportedFile(String fileName, File reportDirectory) throws IOException {
		String resourcePath = "supporting/" + fileName;
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
		
		if(in != null) {
			fileName = StringUtils.replace(fileName, "/", File.separator);
	
			File file = new File(reportDirectory + File.separator + fileName);
			File dirFile = new File(StringUtils.substringBeforeLast(file.getAbsolutePath(), File.separator));
			FileUtils.forceMkdir(dirFile);
	
			IOUtils.copy(in, new FileOutputStream(file));
		} else {
			LOG.warn("Could not find resource: " + resourcePath);
		}
	}
	
	protected void writeStaticSummaryReport(ArchiveReport archive, File reportDirectory) {
		SummaryHtmlWriter writer = new SummaryHtmlWriter();
		
		File report = new File(reportDirectory.getAbsoluteFile() + File.separator + "index.html");
		if(LOG.isDebugEnabled()) {
			LOG.debug("Report: "+report.getAbsolutePath());
		}
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(report);
			writer.writeStatic(fw, archive);
		} catch (IOException e) {
			LOG.error("Exception writing overview.", e);
		}
		finally {
			IOUtils.closeQuietly(fw);
		}
	}
	
	
	protected void writeStaticResourceReports(ArchiveReport archive, File reportDirectory) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Recursing ArchiveMetadata: "+archive.getRelativePathFromRoot());
		}
		
		
		for(ArchiveReport report : archive.getNestedArchiveReports()) {
			//recurse.
			writeStaticResourceReports(report, reportDirectory);
		}
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("Processing ArchiveMetadata: "+archive.getRelativePathFromRoot());
		}
		
		for(ResourceReport resourceReport : archive.getAdditionalInformationReports()) {
			LOG.debug("Generating additional report: "+resourceReport.getClass());
			if(resourceReport instanceof ClasspathReport) {
				ClasspathReport cpr = (ClasspathReport)resourceReport;
				ClassloaderHtmlWriter writer = new ClassloaderHtmlWriter();
				
				if(LOG.isDebugEnabled()) {
					LOG.debug("Report Directory: "+reportDirectory.getAbsoluteFile());
					LOG.debug("Relative Path: "+resourceReport.getRelativePathFromRoot());
				}
				
				String filePath = reportDirectory.getAbsoluteFile() + File.separator + "CLASSLOADER";
				String htmlPath = filePath + ".html";
				cpr.setRelativePathToRoot("./");
				
				if(LOG.isDebugEnabled()) {
					LOG.debug("File Path: "+filePath);
					LOG.debug("HTML Path: "+htmlPath);
				}
				
				File html = new File(htmlPath);
				FileWriter resourceReportFile = null;
				try {
					resourceReportFile = new FileWriter(html);
					
					if(LOG.isDebugEnabled()) {
						LOG.debug("Writing Report: "+htmlPath);
					}
					writer.writeStatic(resourceReportFile, cpr);
					
					String relativePath = ReportUtil.calculateRelativePathFromRoot(reportDirectory, html);
					resourceReport.setRelativePathFromRootToReport(relativePath);
					
				} catch (IOException e1) {
					LOG.error("Exception writing: "+html.getAbsolutePath(), e1);
				} finally {
					IOUtils.closeQuietly(resourceReportFile);
				}
			}
		}

		
		
		
		
		for(ResourceReport resourceReport : archive.getResourceReports()) {
			//convert file resource to meta object and persist.
			
			if(LOG.isDebugEnabled()) {
				LOG.debug("Report Directory: "+reportDirectory.getAbsoluteFile());
				LOG.debug("Relative Path: "+resourceReport.getRelativePathFromRoot());
			}
			
			String filePath = reportDirectory.getAbsoluteFile() + File.separator + resourceReport.getRelativePathFromRoot();
			String htmlPath = filePath + ".html";

			if(LOG.isDebugEnabled()) {
				LOG.debug("File Path: "+filePath);
				LOG.debug("HTML Path: "+htmlPath);
			}
			
			File html = new File(htmlPath);
			FileWriter resourceReportFile = null;
			try {
				resourceReportFile = new FileWriter(html);
				ResourceHtmlWriter writer = new ResourceHtmlWriter();
				
				if(LOG.isDebugEnabled()) {
					LOG.debug("Writing Report: "+htmlPath);
				}
				String sourceText = FileUtils.readFileToString(new File(filePath));
				writer.writeStatic(resourceReportFile, sourceText, resourceReport);
				
				String relativePath = ReportUtil.calculateRelativePathFromRoot(reportDirectory, html);
				resourceReport.setRelativePathFromRootToReport(relativePath);
				
			} catch (IOException e1) {
				LOG.error("Exception writing: "+html.getAbsolutePath(), e1);
			} finally {
				IOUtils.closeQuietly(resourceReportFile);
			}
		}
	}
	
	protected ArchiveReport toArchiveReport(ArchiveMetadata archive, File reportDirectory) {
		ArchiveMetaTransformer fmt = new ArchiveMetaTransformer();
		ArchiveReport report = (ArchiveReport)fmt.toResourceReport(archive, reportDirectory, null);
		 
		for(ArchiveMetadata meta : archive.getNestedArchives()) {
			ArchiveReport temp = toArchiveReport(meta, reportDirectory);
			report.getNestedArchiveReports().add(temp);
		}
		
		for(FileMetadata fileMeta : archive.getEntries()) {
			MetaResultTransformer transformer = resolver.resolveTransformer(fileMeta.getClass());
			ResourceReport data = transformer.toResourceReport(fileMeta, reportDirectory, report);
			report.getResourceReports().add(data);
		}
		return report;
	}

}
