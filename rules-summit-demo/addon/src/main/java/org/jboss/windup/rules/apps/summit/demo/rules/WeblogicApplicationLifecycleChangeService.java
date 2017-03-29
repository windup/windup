package org.jboss.windup.rules.apps.summit.demo.rules;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;

public class WeblogicApplicationLifecycleChangeService extends GraphService<WeblogicApplicationLifecycleXMLChangeModel>
{
	public WeblogicApplicationLifecycleChangeService(GraphContext context) {
		super(context, WeblogicApplicationLifecycleXMLChangeModel.class);
	}
}