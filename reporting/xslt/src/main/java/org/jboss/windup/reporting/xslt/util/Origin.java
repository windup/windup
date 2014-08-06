package org.jboss.windup.reporting.xslt.util;

import java.io.File;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *  Information about where the given piece of config data came from.
 *  Typically, it would keep a file info, but also server info, part of XML, etc.
 * 
 *  @Jira:  MIGR-109  Config (JAXB) beans to remember where did they come from.
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
@XmlAccessorType( XmlAccessType.NONE )
public class Origin {
    
    @XmlAttribute private String server;
    @XmlAttribute private File file;
    @XmlAttribute private String part;
    @XmlAttribute private Integer offset;

    /** Currently, all the data we know come from files. */
    public Origin( File file ) {
        this.file = file;
    }

    public Origin( File docFile, String part ) {
        this.file = docFile;
        this.part = part;
    }

    private Origin() { } // JAXB


    /**
     *  Returns "foo/bar.xml @ /xpath/expression #line-or-order from my.server.com".
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if( file != null ) sb.append( file.getPath() );
        if( part != null ) sb.append(" @ ").append(part);
        if( offset != null ) sb.append(" #").append(offset);
        if( server != null ) sb.append(" from ").append(server);
        return sb.toString();
    }
    
    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getServer() { return server; }
    public Origin setServer( String server ) { this.server = server; return this; }
    public File getFile() { return file; }
    public Origin setFile( File file ) { this.file = file; return this; }
    public String getPart() { return part; }
    public Origin setPart( String part ) { this.part = part; return this; }
    public Integer getOffset() { return offset; }
    public Origin setOffset( Integer offset ) { this.offset = offset; return this; }
    //</editor-fold>
    
    
    /**
     *  For config data beans which know where did they come from.
     */
    public interface Wise {
        Origin getOrigin();
        Object setOrigin( Origin origin );
    }    
    
    
}// class
