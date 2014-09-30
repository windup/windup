package com.tinkerpop.frames.modules.typedgraph;

import java.util.HashMap;
import java.util.Map;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.util.Validate;

/**
 * A TypeRegistry for a {@link FramedGraph}. With a {@link TypeRegistry} the {@link FramedGraph} is able to store type-information on edges,
 * and use stored type-information to construct vertices/edges based on type-information stored in the graph (runtime).
 * 
 * @see TypeField
 * @see TypeValue
 * @see FramedGraph
 */
public class TypeRegistry {
	Map<Class<?>, Class<?>> typeFields = new HashMap<Class<?>, Class<?>>();
	Map<TypeDiscriminator, Class<?>> typeDiscriminators = new HashMap<TypeDiscriminator, Class<?>>();
	
	
	
	/**
	 * @return The interface that has the {@link TypeField} annotation for this class. (Either the class itself, or a base class if the class was registered).
	 */
	public Class<?> getTypeHoldingTypeField(Class<?> type) {
		if (type.getAnnotation(TypeField.class) != null)
			return type;
		return typeFields.get(type);
	}
	
	/**
	 * @param typeHoldingTypeField The type that has the {@link TypeField} annotation for the Proxy class to be constructed. 
	 * @param typeValue The actual persisted value for a type field on an {@link Element} in the graph
	 * @return The type that needs to be constructed, or null if there is no registered class that matches the input values.
	 */
	public Class<?> getType(Class<?> typeHoldingTypeField, String typeValue) {
		Class<?> result = typeDiscriminators.get(new TypeDiscriminator(typeHoldingTypeField, typeValue));
		return result;
	}
	
	static final class TypeDiscriminator {
		private Class<?> typeHoldingTypeField;
		private String value;
		
		TypeDiscriminator(Class<?> typeHoldingTypeField, String value) {
			Validate.assertNotNull(typeHoldingTypeField, value);
			this.typeHoldingTypeField = typeHoldingTypeField;
			this.value = value;
		}

		@Override public int hashCode() {
			return 31 * (31 + typeHoldingTypeField.hashCode()) + value.hashCode();
		}

		@Override public boolean equals(Object obj) {
			if (obj instanceof TypeDiscriminator) {
				TypeDiscriminator other = (TypeDiscriminator)obj;
				//fields are never null:
				return typeHoldingTypeField.equals(other.typeHoldingTypeField) && value.equals(other.value);
			}
			return false;
		}
	}

	/**
	 * @param Add the interface to the registry. The interface should have a {@link TypeValue} annotation, and there should be a {@link TypeField} annotation
	 * on the interface or its parents.
	 */
	public TypeRegistry add(Class<?> type) {
		Validate.assertArgument(type.isInterface(), "Not an interface: %s", type.getName());
		if (!typeFields.containsKey(type)) {
			Class<?> typeHoldingTypeField = findTypeHoldingTypeField(type);
			Validate.assertArgument(typeHoldingTypeField != null, "The type and its supertypes don't have a @TypeField annotation: %s", type.getName());
			typeFields.put(type, typeHoldingTypeField);
			registerTypeValue(type, typeHoldingTypeField);
		}
		return this;
	}
	
	

	
	private Class<?> findTypeHoldingTypeField(Class<?> type) {
		Class<?> typeHoldingTypeField = type.getAnnotation(TypeField.class) == null ? null : type;
		for (Class<?> parentType: type.getInterfaces()) {
			Class<?> parentTypeHoldingTypeField = findTypeHoldingTypeField(parentType);
			Validate.assertArgument(parentTypeHoldingTypeField == null || typeHoldingTypeField == null || parentTypeHoldingTypeField == typeHoldingTypeField, "You have multiple TypeField annotations in your class-hierarchy for %s", type.getName());
			if (typeHoldingTypeField == null)
				typeHoldingTypeField = parentTypeHoldingTypeField;
		}
		return typeHoldingTypeField;
	}
	
	private void registerTypeValue(Class<?> type, Class<?> typeHoldingTypeField) {
		TypeValue typeValue = type.getAnnotation(TypeValue.class);
		Validate.assertArgument(typeValue != null, "The type does not have a @TypeValue annotation: %s", type.getName());
		typeDiscriminators.put(new TypeRegistry.TypeDiscriminator(typeHoldingTypeField, typeValue.value()), type);
	}
}
