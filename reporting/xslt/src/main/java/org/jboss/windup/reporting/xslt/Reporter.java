package org.jboss.windup.reporting.xslt;


import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.FileUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ApplicationModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.xslt.model.MigrationReportJaxbBean;
import org.jboss.windup.reporting.xslt.util.XmlUtils;
import org.jboss.windup.util.exception.WindupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 *  Extracts report data from MigrationContext and dumps them to a XML file.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Reporter {
    public static final Logger log = LoggerFactory.getLogger( Reporter.class );
    
    private static final String RESOURCES_PATH = "/org/jboss/windup/reporting/xslt/";
    private static final String XSLT_FILE = "MigrationReportJaxbBean.xsl";
    private static final String CSS_FILE = "MigrationReport.css";
    private static final String JQUERY_FILE = "jquery-1.10.1.min.js";

    
    public static void createReport( GraphContext ctx, File reportDir ) throws WindupException {
        try {
            // Create the reporting content.
            MigrationReportJaxbBean report = new MigrationReportJaxbBean();
            report.config = GraphService.getConfigurationModel(ctx);
            //report.finalException = ctx.getFinalException();
            
            // Copy deployments reports to the $reportDir.
            for( ApplicationModel depl : report.deployments ) {
                // TODO, or remove - should be generic.
            }
            
            Marshaller mar = XmlUtils.createMarshaller( MigrationReportJaxbBean.class );
            
            // File name
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
            File reportFile = new File(reportDir, "MigrationReport-"+timestamp+".xml");
            FileUtils.forceMkdir( reportDir );
            
            // Write to a file.
            //log.debug("Writing the report to " + reportFile.getPath());
            //mar.marshal( report, reportFile );
            
            // Write to a Node.
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            mar.marshal( report, doc );
            
            // Write node to a file.
            log.debug("Storing the XML report to " + reportFile.getPath());
            XmlUtils.saveXmlToFile( doc, reportFile );
            
            // Use XSLT to produce HTML report.
            File htmlFile = new File( reportFile.getPath() + ".html");
            log.debug("Storing the HTML report to " + htmlFile.getPath());
            InputStream is = Reporter.class.getResourceAsStream(RESOURCES_PATH + XSLT_FILE);
            XmlUtils.transformDocToFile( doc, htmlFile, is );
            
            // Copy CSS and jQuery.
            is = Reporter.class.getResourceAsStream(RESOURCES_PATH + CSS_FILE);
            FileUtils.copyInputStreamToFile( is, new File(reportDir, CSS_FILE) );
            is = Reporter.class.getResourceAsStream(RESOURCES_PATH + JQUERY_FILE);
            FileUtils.copyInputStreamToFile( is, new File(reportDir, "jQuery.js") );
            is = Reporter.class.getResourceAsStream(RESOURCES_PATH + "iconsBig.png");
            FileUtils.copyInputStreamToFile( is, new File(reportDir, "iconsBig.png") );
            is = Reporter.class.getResourceAsStream(RESOURCES_PATH + "iconsMed.png");
            FileUtils.copyInputStreamToFile( is, new File(reportDir, "iconsMed.png") );
        }
        /*catch( TransformerException ex ){
            //log.error("ex:", ex);
            //log.error("ex.getCause():", ex.getCause());
            //log.error("ex.getException():", ex.getException());
            for( Throwable throwable : ex.getSuppressed() ) {
                log.error( "ex.getSuppressed():", new Exception( throwable ) );
            }
            throw new WindupException("Failed creating migration report:\n    " + ex.getMessageAndLocation(), ex);
        }*/
        catch( Exception ex ) {
            final String msg = "Failed creating migration report:\n    " + ex.getMessage();
            log.error(msg, ex);
            throw new WindupException(msg, ex);
        }
    }
    
    
}// class
