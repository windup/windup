package org.jboss.windup.reporting.structure;

import java.io.Writer;

public abstract class Page implements HtmlSerializable {
	private String title;

	@Override
	public void toHtml(Writer writer) {
		
	}

}
