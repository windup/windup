package org.jboss.windup.reporting.structure;

import java.io.Writer;
import java.util.List;

public class Menu implements HtmlSerializable {
	private List<Page> pages;
	
	public List<Page> getPages() {
		return pages;
	}
	
	public void setPages(List<Page> pages) {
		this.pages = pages;
	}
	
	@Override
	public void toHtml(Writer writer) {
		//whatever.
	}
}
