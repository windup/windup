package org.jboss.windup.decorator.xml;

import org.apache.commons.lang.text.StrSubstitutor;
import org.jboss.windup.hint.ResultProcessor;
import org.jboss.windup.resource.decoration.AbstractDecoration;
import org.jboss.windup.resource.decoration.XmlLine;
import org.w3c.dom.Node;

public class XmlMarkdownProcessor implements ResultProcessor {

	private String markdown;
	
	public void setMarkdown(String markdown) {
		this.markdown = markdown;
	}
	
	@Override
	public void process(AbstractDecoration result) {
		XmlLine line = (XmlLine)result;
		Node node = line.getMatchedNode();
		
		XPathPropertyReplacer xpr = new XPathPropertyReplacer();
		StrSubstitutor xpathSubstitutor = new StrSubstitutor(xpr);
	}
}
