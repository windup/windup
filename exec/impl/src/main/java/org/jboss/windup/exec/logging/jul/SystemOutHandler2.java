/*
 * @(#)SystemOutHandler.java	1.13 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.jboss.windup.exec.logging.jul;

import java.util.logging.*;

/**
 * This <tt>Handler</tt> publishes log records to <tt>System.err</tt>.
 * By default the <tt>SimpleFormatter</tt> is used to generate brief summaries.
 * <p>
 * <b>Configuration:</b>
 * By default each <tt>ConsoleHandler</tt> is initialized using the following
 * <tt>LogManager</tt> configuration properties.  If properties are not defined
 * (or have invalid values) then the specified default values are used.
 * <ul>
 * <li>   java.util.logging.ConsoleHandler.level 
 *	  specifies the default level for the <tt>Handler</tt>
 *	  (defaults to <tt>Level.INFO</tt>).
 * <li>   java.util.logging.ConsoleHandler.filter
 *	  specifies the name of a <tt>Filter</tt> class to use
 *	  (defaults to no <tt>Filter</tt>).
 * <li>   java.util.logging.ConsoleHandler.formatter
 * 	  specifies the name of a <tt>Formatter</tt> class to use
 *  	  (defaults to <tt>java.util.logging.SimpleFormatter</tt>).
 * <li>   java.util.logging.ConsoleHandler.encoding 
 *	  the name of the character set encoding to use (defaults to
 *	  the default platform encoding).
 * </ul>
 * <p>
 * @version 1.13, 11/17/05
 * @since 1.4
 */
public class SystemOutHandler2 extends StreamHandler {
  // Private method to configure a ConsoleHandler from LogManager
  // properties and/or default values as specified in the class
  // javadoc.
  private void configure() {
    LogManager manager = LogManager.getLogManager();
    String cname = getClass().getName();

    Level level;
    try {
      level = Level.parse(manager.getProperty(cname + ".level"));
    } catch (Exception ex) {
      level = Level.INFO;
    }
    setLevel(level);

    //setFilter(manager.getFilterProperty(cname + ".filter", null));
    setFormatter( getFormatterProperty(cname + ".formatter", new SimpleFormatter()) );
    try {
      setEncoding( manager.getProperty(cname + ".encoding") );
    } catch (Exception ex) {
      try {
        setEncoding(null);
      } catch (Exception ex2) {
      // doing a setEncoding with null should always work.
      // assert false;
      }
    }
  }

  Formatter getFormatterProperty(String name, Formatter defaultValue) {
    LogManager manager = LogManager.getLogManager();
    String val = manager.getProperty(name);
    try {
      if (val != null) {
        Class clz = ClassLoader.getSystemClassLoader().loadClass(val);
        return (Formatter) clz.newInstance();
      }
    } catch (Exception ex) {
    // We got one of a variety of exceptions in creating the
    // class or creating an instance.
    // Drop through.
    }
    // We got an exception.  Return the defaultValue.
    return defaultValue;
  }

  /**
   * Create a <tt>ConsoleHandler</tt> for <tt>System.err</tt>.
   * <p>
   * The <tt>ConsoleHandler</tt> is configured based on
   * <tt>LogManager</tt> properties (or their default values).
   * 
   */
  public SystemOutHandler2() {
  //  sealed = false;
    configure();
    setOutputStream(System.err);
  //sealed = true;
  }

  /**
   * Publish a <tt>LogRecord</tt>.
   * <p>
   * The logging request was made initially to a <tt>Logger</tt> object,
   * which initialized the <tt>LogRecord</tt> and forwarded it here.
   * <p>
   * @param  record  description of the log event. A null record is
   *                 silently ignored and is not published
   */
  public void publish(LogRecord record) {
    super.publish(record);
    flush();
  }

  /**
   * Override <tt>StreamHandler.close</tt> to do a flush but not
   * to close the output stream.  That is, we do <b>not</b>
   * close <tt>System.err</tt>.
   */
  public void close() {
    flush();
  }
}

