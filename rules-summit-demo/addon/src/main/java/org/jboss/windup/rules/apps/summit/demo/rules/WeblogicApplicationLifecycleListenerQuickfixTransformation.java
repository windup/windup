package org.jboss.windup.rules.apps.summit.demo.rules;

import org.jboss.windup.reporting.quickfix.QuickfixLocationDTO;
import org.jboss.windup.reporting.quickfix.QuickfixTransformation;

public class WeblogicApplicationLifecycleListenerQuickfixTransformation implements QuickfixTransformation
{
	public static final String ID = WeblogicApplicationLifecycleListenerQuickfixTransformation.class.getSimpleName();
	
	@Override
	public String getTransformationID() {
		return ID;
	}
	
	@Override
	public String transform(QuickfixLocationDTO locationDTO) {
		return null;
	}
}
