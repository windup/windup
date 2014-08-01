package org.jboss.windup.log.jul.format;

import java.util.logging.*;

/**
 * This <tt>Handler</tt> publishes log records to <tt>System.out</tt>.
 * Similar to java.util.logging.ConsoleHandler.
 */
public class SystemOutHandler2 extends StreamHandler {
    // Private method to configure a ConsoleHandler from LogManager
    // properties and/or default values as specified in the class javadoc.

    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        Level level;
        try {
            level = Level.parse( manager.getProperty( cname + ".level" ) );
        } catch( Exception ex ) {
            level = Level.INFO;
        }
        setLevel( level );

        //setFilter(manager.getFilterProperty(cname + ".filter", null));
        setFormatter( getFormatterProperty( cname + ".formatter", new SimpleFormatter() ) );
        try {
            setEncoding( manager.getProperty( cname + ".encoding" ) );
        } catch( Exception ex ) {
            try {
                setEncoding( null );
            } catch( Exception ex2 ) {
            }
        }
    }


    Formatter getFormatterProperty( String name, Formatter defaultValue ) {
        LogManager manager = LogManager.getLogManager();
        String val = manager.getProperty( name );
        try {
            if( val != null ) {
                Class clz = ClassLoader.getSystemClassLoader().loadClass( val );
                return (Formatter) clz.newInstance();
            }
        } catch( Exception ex ) {
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
        configure();
        setOutputStream( System.out );
    }


    /**
     * Publish a <tt>LogRecord</tt>.
     * <p>
     * The logging request was made initially to a <tt>Logger</tt> object, which
     * initialized the <tt>LogRecord</tt> and forwarded it here.
     * <p>
     * @param record description of the log event. A null record is silently
     * ignored and is not published
     */
    public void publish( LogRecord record ) {
        super.publish( record );
        flush();
    }


    /**
     * Override <tt>StreamHandler.close</tt> to do a flush but not to close the
     * output stream. That is, we do <b>not</b>
     * close <tt>System.err</tt>.
     */
    public void close() {
        flush();
    }
}
