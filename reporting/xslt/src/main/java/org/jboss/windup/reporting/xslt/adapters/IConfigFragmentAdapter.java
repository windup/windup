package org.jboss.windup.reporting.xslt.adapters;


import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.windup.reporting.xslt.beans.ConfigFragmentReportBean;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class IConfigFragmentAdapter extends XmlAdapter<ConfigFragmentReportBean, IConfigFragment> {

    @Override
    public ConfigFragmentReportBean marshal( IConfigFragment fragment ) throws Exception {
        if( fragment == null )  return null;
        return ConfigFragmentReportBean.from( fragment );
    }
    
    @Override
    public IConfigFragment unmarshal( ConfigFragmentReportBean v ) throws Exception {
        throw new UnsupportedOperationException( "Unmarshalling not supported." );
    }

}// class
