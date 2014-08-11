package org.jboss.windup.log.jul.format;

import java.util.logging.*;
import java.io.*;
import java.text.*;
import java.util.Date;

/**
 * Print a brief summary of the LogRecord on one line.
 */
public class SimplestFormatter extends Formatter {

  Date dat = new Date();
  private Object args[] = new Object[1];

  private static final String NL = "\n";
  

  /**
   * Format the given LogRecord.
   * @param record the log record to be formatted.
   * @return a formatted log record
   */
  public synchronized String format(LogRecord record) {

        StringBuilder sb = new StringBuilder();

        // Level
        Level level = record.getLevel();
        if( level == Level.WARNING ){
            sb.append("WARN:  ");
        }
        else if( level == Level.SEVERE ){
            sb.append("ERROR: ");
        }
        else
            sb.append("       ");


        // Single message
        String message = formatMessage(record);
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
