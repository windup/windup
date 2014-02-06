package org.jboss.windup.config.condition;

import javax.xml.xpath.XPathExpression;

import org.apache.commons.lang.NotImplementedException;


public class XPathExistsCondition<T> implements Condition<T> {

	protected XPathExpression xpathExpression; 
	
	@Override
	public boolean match(T obj) {
		throw new NotImplementedException("Not yet implemented.");
	}
	
	public void setXpathExpression(XPathExpression xpathExpression) {
		this.xpathExpression = xpathExpression;
	}
	
	public XPathExpression getXpathExpression() {
		return xpathExpression;
	}

	@Override
	public String toString() {
		return "XPathExistsCondition [xpathExpression=" + xpathExpression + "]";
	}
}
