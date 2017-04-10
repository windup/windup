package org.jboss.windup.tooling.data;

import java.util.List;

public interface TransformationQuickfix extends Quickfix 
{
	void addChange(TransformationQuickfixChange change);
	List<TransformationQuickfixChange> getChanges();
}
