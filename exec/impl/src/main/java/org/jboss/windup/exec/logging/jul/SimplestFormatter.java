/*
 * @(#)SimpleFormatter.java	1.15 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.jboss.windup.exec.logging.jul;

import java.util.logging.*;
import java.io.*;
import java.text.*;
import java.util.Date;

/**
 * Print a brief summary of the LogRecord in a human readable
 * format.  The summary will typically be 1 or 2 lines.
 *
 * @version 1.15, 11/17/05
 * @since 1.4
 */
public class SimplestFormatter extends Formatter {

  Date dat = new Date();
  private final static String format = "{0,date} {0,time}";
  private MessageFormat formatter;
  private Object args[] = new Object[1];

  // Line separator string.  This is the value of the line.separator
  // property at the moment that the SimpleFormatter was created.
  //private String lineSeparator = (String) java.security.AccessController.doPrivileged(
  //        new sun.security.action.GetPropertyAction("line.separator"));
  private String lineSeparator = "\n";

  /**
   * Format the given LogRecord.
   * @param record the log record to be formatted.
   * @return a formatted log record
   */
  public synchronized String format(LogRecord record) {
    
    StringBuffer sb = new StringBuffer();
    
    /*
    // Minimize memory allocations here.
    dat.setTime(record.getMillis());    
    args[0] = dat;
    
    // Date and time 
    StringBuffer text = new StringBuffer();
    if (formatter == null) {
      formatter = new MessageFormat(format);
    }
    formatter.format(args, text, null);
    sb.append(text);
    sb.append(" ");
    
    
    // Class name 
    if (record.getSourceClassName() != null) {
      sb.append(record.getSourceClassName());
    } else {
      sb.append(record.getLoggerName());
    }
    
    // Method name 
    if (record.getSourceMethodName() != null) {
      sb.append(" ");
      sb.append(record.getSourceMethodName());
    }
    sb.append(" - "); // lineSeparator
    */
    
    
    
    // Level
    //sb.append(record.getLevel().getLocalizedName());
    Level level = record.getLevel();
    if( level == Level.WARNING ){
      sb.append("Varov�n�: ");
    }else if( level == Level.SEVERE ){
      sb.append("Chyba!    ");
    }else
      sb.append("          ");
    

    // Samotn� zpr�va
    String message = formatMessage(record);
    sb.append(message);
    sb.append(lineSeparator);
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
