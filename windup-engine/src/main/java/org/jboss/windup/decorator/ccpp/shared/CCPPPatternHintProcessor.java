package org.jboss.windup.decorator.ccpp.shared;

import org.jboss.windup.hint.RegexPatternHintProcessor;
import org.jboss.windup.resource.decoration.AbstractDecoration;


public class CCPPPatternHintProcessor extends RegexPatternHintProcessor {
	private SourceType sourceType;
	private Language language;
	
	
	public SourceType getSourceType() {
		return sourceType;
	}
	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}
	
	public Language getLanguage() {
		return language;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	@Override
	public void process(AbstractDecoration result) {
		if (result instanceof CCPPLineResult) {
			CCPPLineResult castResult = (CCPPLineResult)result;
			
			if(language == Language.ALL || castResult.getLanguage() == language) {
				super.process(result);
			}
		}
	}
}
