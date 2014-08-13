package org.jboss.windup.log.jul.format;

import java.util.logging.*;
import java.io.*;
import java.text.*;
import java.util.Date;

/**
 * Print a brief summary of the LogRecord on a single line.
 */
public class SingleLineFormatter extends Formatter {

    private final Date date = new Date();
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String NL = "\n";

    /**
     * Format the given LogRecord.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(LogRecord record) {

        StringBuilder sb = new StringBuilder();

        // Date and time 
        // Minimize memory allocations here.
        date.setTime(record.getMillis());    
        sb.append( DATE_FORMAT.format( date ) );
        sb.append(' ');

        // Level
        sb.append(record.getLevel().getLocalizedName());
        sb.append(' ');


        // Class name 
        if (record.getSourceClassName() != null) {
            sb.append(record.getSourceClassName());
        } else {
            sb.append(record.getLoggerName());
        }

        // Method name 
        if (record.getSourceMethodName() != null) {
            sb.append(' ');
            sb.append(record.getSourceMethodName());
        }
        sb.append(": ");



        String message = formatMessage(record);


        // Indent - the more serious, the more indented.
        //sb.append( String.format("% ""s") );
        int iOffset = (1000 - record.getLevel().intValue()) / 100;
        for( int i = 0; i < iOffset;  i++ ){
            sb.append(" ");
        }


        sb.append(message);
        sb.append(NL);
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
            }
        }
        return sb.toString();
    }
}
