package org.jboss.windup.decorator.xml;

import org.apache.commons.lang.text.StrLookup;

public class XPathPropertyReplacer extends StrLookup {

	@Override
	public String lookup(String key) {
		return "Hello: "+key;
	}

}
