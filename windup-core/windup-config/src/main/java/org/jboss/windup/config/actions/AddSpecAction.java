package org.jboss.windup.config.actions;

import org.jboss.windup.config.base.Action;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.meta.SpecificationVersionMeta;
import org.jboss.windup.graph.model.resource.Resource;

public class AddSpecAction<T extends Resource> implements Action<T> {

	private String specificationVersion;
	
	@Override
	public void execute(GraphContext graphContext, T obj, LocalContext localContext) {
		SpecificationVersionMeta specVersion = graphContext.getFramed().addVertex(null, SpecificationVersionMeta.class);
		specVersion.setSpecVersion(specificationVersion);
		obj.addMeta(specVersion);
	}

	public String getSpecificationVersion() {
		return specificationVersion;
	}

	public void setSpecificationVersion(String specificationVersion) {
		this.specificationVersion = specificationVersion;
	}

	@Override
	public String toString() {
		return "AddSpecAction [specificationVersion=" + specificationVersion + "]";
	}
	
	
}
