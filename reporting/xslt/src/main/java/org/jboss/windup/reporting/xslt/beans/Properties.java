package org.jboss.windup.reporting.xslt.beans;

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB (at least MOXy) can't deal with Map -> List adapter, needs a special wrapping object.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
@XmlAccessorType( value = XmlAccessType.FIELD )
public final class Properties {
    
    @XmlElement( name = "property" )
    private List<ReportProperty> props;

    public Properties() {
        props = new LinkedList();
    }

    public Properties( List<ReportProperty> props ) {
        this.props = props;
    }

}// class
