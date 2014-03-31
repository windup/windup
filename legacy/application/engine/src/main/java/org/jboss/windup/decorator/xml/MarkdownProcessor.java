package org.jboss.windup.decorator.xml;

import org.apache.commons.lang.text.StrSubstitutor;
import org.jboss.windup.hint.ResultProcessor;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.hint.MarkdownHint;

public class MarkdownProcessor implements ResultProcessor {

	private String markdown;
	
	public void setMarkdown(String markdown) {
		this.markdown = markdown;
	}
	
	@Override
	public void process(AbstractDecoration result) {
		StrSubstitutor xpathSubstitutor = new StrSubstitutor(result.getContext());
		String rst = xpathSubstitutor.replace(markdown);
		
		MarkdownHint mdh = new MarkdownHint();
		mdh.setMarkdown(rst);
		result.getHints().add(mdh);
	}
}
