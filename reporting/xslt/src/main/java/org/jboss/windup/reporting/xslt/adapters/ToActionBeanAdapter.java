package org.jboss.windup.reporting.xslt.adapters;


import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.windup.reporting.xslt.beans.ActionBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ToActionBeanAdapter extends XmlAdapter<ActionBean, IMigrationAction> {
    private static final Logger log = LoggerFactory.getLogger( ToActionBeanAdapter.class );


    @Override
    public ActionBean marshal( IMigrationAction action ) throws Exception {
        if( action == null){
            log.warn("Null action passed to marshal().");
            return null;
        }

        ActionBean ret = ActionBean.from( action );
        return ret;
    }

    @Override
    public IMigrationAction unmarshal( ActionBean v ) throws Exception {
        throw new UnsupportedOperationException("Unmarshalling not supported.");
    }

}// class
