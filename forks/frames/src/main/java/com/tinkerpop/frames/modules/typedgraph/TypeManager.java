package com.tinkerpop.frames.modules.typedgraph;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.EdgeFrame;
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.TypeResolver;

public class TypeManager implements TypeResolver, FrameInitializer {

	private TypeRegistry typeRegistry;
    public TypeManager(TypeRegistry typeRegistry) {
    	this.typeRegistry = typeRegistry;
    }
    
	@Override public Class<?>[] resolveTypes(Vertex v, Class<?> defaultType) {
		return new Class<?>[] {resolve(v, defaultType), VertexFrame.class };
	}

	@Override public Class<?>[] resolveTypes(Edge e, Class<?> defaultType) {
		return new Class<?>[] {resolve(e, defaultType), EdgeFrame.class };
	}
	
	private Class<?> resolve(Element e, Class<?> defaultType) {
		Class<?> typeHoldingTypeField = typeRegistry.getTypeHoldingTypeField(defaultType);
		if (typeHoldingTypeField != null) {
			String value = e.getProperty(typeHoldingTypeField.getAnnotation(TypeField.class).value());
			Class<?> type = value == null ? null : typeRegistry.getType(typeHoldingTypeField, value);
			if (type != null) {
				return type;
			}
		}
		return defaultType;
	}

	@Override
	public void initElement(Class<?> kind, FramedGraph<?> framedGraph, Element element) {
		Class<?> typeHoldingTypeField = typeRegistry.getTypeHoldingTypeField(kind);
		if (typeHoldingTypeField != null) {
			TypeValue typeValue = kind.getAnnotation(TypeValue.class);
			if (typeValue != null) {
				element.setProperty(typeHoldingTypeField.getAnnotation(TypeField.class).value(), typeValue.value());
			}
		}
	}


}
