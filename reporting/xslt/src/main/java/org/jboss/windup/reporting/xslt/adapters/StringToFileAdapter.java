package org.jboss.windup.reporting.xslt.adapters;


import java.io.File;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 *  @deprecated  File conversion is done automatically.
 */
public class StringToFileAdapter extends XmlAdapter<String, File> {
    private static final Logger log = LoggerFactory.getLogger( StringToFileAdapter.class );


    @Override
    public String marshal( File v ) throws Exception {
        return v.getPath();
    }


    @Override
    public File unmarshal( String v ) throws Exception {
        return new File(v);
    }

}// class
