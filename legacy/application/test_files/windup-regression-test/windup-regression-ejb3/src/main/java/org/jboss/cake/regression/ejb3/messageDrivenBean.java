package com.jboss.windup.regression.ejb3;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.Stateful;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ejb.EJBLocalHome;

/**
 * Message-Driven Bean implementation class for: messageDrivenBean
 *
 */
@Stateful
@MessageDriven(
		activationConfig = { @ActivationConfigProperty(
				propertyName = "destinationType", propertyValue = "javax.jms.Queue"
		) })
public class messageDrivenBean implements MessageListener {

    /**
     * Default constructor. 
     * @throws NamingException 
     */
    public messageDrivenBean() throws NamingException {
    	 InitialContext ic = new InitialContext();
         EJBLocalHome home = (EJBLocalHome)ic.lookup( "java:comp/env/ejb/MyEJBBean" );
         
    }
	
	/**
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {

    }

}
