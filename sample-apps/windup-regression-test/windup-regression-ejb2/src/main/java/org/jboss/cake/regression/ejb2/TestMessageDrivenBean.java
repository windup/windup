package com.jboss.windup.regression.ejb2;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;

public class TestMessageDrivenBean implements MessageDrivenBean{

	public void ejbRemove() throws EJBException {
		
	}

	public void setMessageDrivenContext(MessageDrivenContext arg0)
			throws EJBException {
		
	}

}
