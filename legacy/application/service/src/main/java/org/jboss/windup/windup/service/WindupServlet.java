package org.jboss.windup.windup.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.WindupEngine;
import org.jboss.windup.WindupEnvironment;
import org.jboss.windup.reporting.ReportEngine;

/**
 *  This servlet is the one which serves as the input form and processes the uploaded file.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@WebServlet( name = "WindupServlet", urlPatterns = { "/WindupServlet" } )
public class WindupServlet extends HttpServlet {

    public static final String REPORTS_DIR_PREFIX = "JBossMigration-WindUpReports-";

    
    /**
     * Handles the HTTP <code>GET</code> method - initial request without input.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException 
    {
        response.setContentType( "text/html;charset=UTF-8" );
        
        try(PrintWriter out = response.getWriter()) {
            printBeginStuff( out );
            out.println("<h3>Choose the archive to analyze with WindUp:</h3>"
                    + "<form method='post' enctype='multipart/form-data'>"
                    + "    <input type='file' name='file' />"
                    + "    <input type='submit' value='Analyze!' onclick='this.form.disabled=true;' />"
                    + "</form>");
            printEndStuff( out );
        }
    }

    
    /**
     * Handles the HTTP <code>POST</code> method - when the user submitted the form.
     */
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException 
    {
        response.setContentType( "text/html;charset=UTF-8" );
        
        Throwable error = null;
        
        Part part = request.getPart("file");
        
        // Only .war or .ear etc.
        if( ! part.getSubmittedFileName().toLowerCase().endsWith(".war") ){
            error = new IllegalArgumentException("Not a supported type: " + part.getSubmittedFileName());
        }
        String archiveBaseName = Paths.get( part.getSubmittedFileName() ).getFileName().toString();
        archiveBaseName = normalizeFileName( archiveBaseName );
        
        // WindUp
        final WindupEnvironment windupEnv = new WindupEnvironment();
        final WindupEngine windupEng = new WindupEngine( windupEnv );
        final ReportEngine windupReport = new ReportEngine( windupEnv );

        // Create a temp dir for the report dirs.
        File reportsTmpDir = Files.createTempDirectory(REPORTS_DIR_PREFIX).toFile();
        String reportsTmpDir_suffix = reportsTmpDir.getName().substring( REPORTS_DIR_PREFIX.length() );
        
        File depl = new File(reportsTmpDir, archiveBaseName);
        FileUtils.copyInputStreamToFile( part.getInputStream(), depl );
        
        // Process with WindUp
        File reportTmpDir = reportsTmpDir; //new File(reportsTmpDir, depl.getName() );
        windupEnv.setInputPath(depl);
        windupEnv.setOutputPath(reportsTmpDir);
        try {
            windupReport.process();
        }
        catch( Throwable ex ){
            error = ex;
        }
        
        if( error != null ){
            response.setStatus(400);
        }
        
        try(PrintWriter out = response.getWriter()) {
            printBeginStuff( out );
            
            if( error != null ){
                out.print( "<div class='error'>Error: " + error.getMessage() + "</div>" );
            }
            else {
                out.print( "<a href='WindupReport/"+reportsTmpDir_suffix+"'>Go to the WindUp report</a>" );
            }
            printEndStuff( out );
        }
    }


    private void printEndStuff( final PrintWriter out ) {
        out.println( "</body></html>" );
    }





    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>


    private String normalizeFileName( String fileName  ) {
        return fileName = StringUtils.replaceChars( fileName, "/><\"\'?*", "_________");
    }


    private void printBeginStuff( PrintWriter out ) {
        out.println( "<!DOCTYPE html>" );
        out.println( "<html><head>" );
        out.println( "    <title>WindUp as a Service 0.0.1</title>" );            
        out.println( "</head><body>" );
        out.println( "    <h1>WindUp as a Service 0.0.1</h1>" );
    }

    
}// class
