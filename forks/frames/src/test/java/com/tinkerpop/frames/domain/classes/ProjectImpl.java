package com.tinkerpop.frames.domain.classes;

public abstract class ProjectImpl implements Project {
	@Override
	public String getLanguageUsingMixin() {
	
		return getLanguage();
	}
}
