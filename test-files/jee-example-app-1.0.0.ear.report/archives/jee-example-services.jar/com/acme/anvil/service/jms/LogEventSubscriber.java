package com.acme.anvil.service.jms;

import javax.jms.JMSException;
import com.acme.anvil.vo.LogEvent;
import javax.jms.ObjectMessage;
import javax.jms.Message;
import java.text.SimpleDateFormat;
import org.apache.log4j.Logger;
import weblogic.ejbgen.MessageDriven;
import javax.jms.MessageListener;
import javax.ejb.MessageDrivenBean;
import weblogic.ejb.GenericMessageDrivenBean;

@MessageDriven(ejbName="LogEventSubscriber",destinationJndiName="jms/LogEventQueue",destinationType="javax.jms.Topic",runAsPrincipalName="anvil_user",runAs="anvil_user")
public class LogEventSubscriber extends GenericMessageDrivenBean implements MessageDrivenBean,MessageListener{
    private static final Logger LOG;
    private static final SimpleDateFormat SDF;
    public void onMessage(final Message msg){
        final ObjectMessage om=(ObjectMessage)msg;
        try{
            final Object obj=om.getObject();
            if(obj instanceof LogEvent){
                final LogEvent event=(LogEvent)obj;
                LogEventSubscriber.LOG.info((Object)("Log Event ["+LogEventSubscriber.SDF.format(event.getDate())+"] : "+event.getMessage()));
            }
        }
        catch(JMSException e){
            LogEventSubscriber.LOG.error((Object)"Exception reading message.",(Throwable)e);
        }
    }
    static{
        LOG=Logger.getLogger((Class)LogEventSubscriber.class);
        SDF=new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm:ss z");
    }
}
