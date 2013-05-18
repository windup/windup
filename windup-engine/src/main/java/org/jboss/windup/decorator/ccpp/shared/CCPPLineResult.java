package org.jboss.windup.decorator.ccpp.shared;

import org.jboss.windup.decorator.ccpp.shared.Language;
import org.jboss.windup.decorator.ccpp.shared.SourceType;
import org.jboss.windup.resource.decoration.Line;

public class CCPPLineResult extends Line {
	private Language language;
	private SourceType sourceType;
	
	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public SourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}
}
