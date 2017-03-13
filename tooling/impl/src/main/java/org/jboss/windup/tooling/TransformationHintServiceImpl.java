package org.jboss.windup.tooling;

import java.util.List;
import java.util.Map;

import org.jboss.windup.tooling.data.Hint;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class TransformationHintServiceImpl implements TransformationHintService
{
	private List<Hint> hints = Lists.newArrayList();
	
	public void addHint(Hint hint)
	{
		hints.add(hint);
	}

	@Override
	public List<Hint> getHints() 
	{
		return hints;
	}
}
