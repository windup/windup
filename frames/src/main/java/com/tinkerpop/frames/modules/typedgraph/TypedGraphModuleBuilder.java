package com.tinkerpop.frames.modules.typedgraph;

import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.modules.AbstractModule;
import com.tinkerpop.frames.modules.Module;

/**
 * TODO
 */
public class TypedGraphModuleBuilder {
	private TypeRegistry typeRegistry = new TypeRegistry();
	
	public TypedGraphModuleBuilder() {
		
	}
	
	public TypedGraphModuleBuilder withClass(Class<?> type) {
		typeRegistry.add(type);
		return this;
	}
	
	public Module build() {
		final TypeManager manager = new TypeManager(typeRegistry);
		return new AbstractModule() {
			
			@Override
			public void doConfigure(FramedGraphConfiguration config) {
				config.addTypeResolver(manager);
				config.addFrameInitializer(manager);
			}			
		};
	}
}
