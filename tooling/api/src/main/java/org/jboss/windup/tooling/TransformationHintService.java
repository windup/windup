package org.jboss.windup.tooling;

import org.jboss.windup.tooling.data.Hint;

public interface TransformationHintService 
{
	void addHint(Hint hint);
	Hint getHints();
}
