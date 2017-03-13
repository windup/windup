package org.jboss.windup.tooling.data;

import java.util.List;

import com.google.common.collect.Lists;

public class TransformationQuickfixImpl extends QuickfixImpl implements TransformationQuickfix
{
	private static final long serialVersionUID = 1L;
	
	private List<TransformationQuickfixChange> changes = Lists.newArrayList();
	
	public void addChange(TransformationQuickfixChange change)
	{
		changes.add(change);
	}

	@Override
	public List<TransformationQuickfixChange> getChanges() 
	{
		return changes;
	}
}
