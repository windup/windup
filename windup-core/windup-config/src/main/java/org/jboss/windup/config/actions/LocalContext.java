package org.jboss.windup.config.actions;

import java.util.HashMap;
import java.util.Map;

public class LocalContext {
	private final LocalContext parent;
	private final Map<String, Object> localContext;
	
	public LocalContext(LocalContext parent) {
		this.parent = parent;
		this.localContext = new HashMap<String, Object>();
	}
	
	public LocalContext getParent() {
		return parent;
	}

	public Map<String, Object> getLocalContext() {
		return localContext;
	}
	
	public Object getVariable(String key) {
		//always delegate to self.
		if(localContext.containsKey(key)) {
			return localContext.get(key);
		}
		else {
			//delegate to parent, if exists..
			if(parent != null) {
				parent.getVariable(key);
			}
		}
		return null;
	}
}
