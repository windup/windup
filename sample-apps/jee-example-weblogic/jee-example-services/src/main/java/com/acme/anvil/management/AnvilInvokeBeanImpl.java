package com.acme.anvil.management;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

/**
 * Provides a standard JMX bean.
 * @author bradsdavis
 */
public class AnvilInvokeBeanImpl extends StandardMBean implements AnvilInvokeBean {

	public AnvilInvokeBeanImpl() throws NotCompliantMBeanException {
		super(AnvilInvokeBean.class);
	}

	private int invoked = 0;
	
	/*
	 * @see com.acme.anvil.management.AnvilInvokeBean#addInvoked()
	 */
	public void addInvoked() {
		invoked++;
	}
	
	/*
	 * @see com.acme.anvil.management.AnvilInvokeBean#getInvoked()
	 */
	public int getInvoked() {
		return invoked;
	}
}
