package org.jboss.windup.rules.apps.java.reporting.rules;

import java.io.IOException;

import org.jboss.windup.reporting.model.DependencyGraphDTO;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ApplicationDependencyGraphDTORelationSerializer extends StdSerializer<DependencyGraphDTO> {

	private static final long serialVersionUID = -3443341738848344508L;
	
	private static final String SOURCE = "source";
	private static final String TARGET = "target";

	protected ApplicationDependencyGraphDTORelationSerializer(Class<DependencyGraphDTO> t) {
		super(t);
	}

	public ApplicationDependencyGraphDTORelationSerializer() {
		this(null);
	}

	@Override
	public void serialize(DependencyGraphDTO dto, JsonGenerator gen, SerializerProvider provider) {
		dto.getParents().forEach( item -> {
			try {
				gen.writeStartObject();
				gen.writeStringField(SOURCE, dto.getSha1());
				gen.writeStringField(TARGET, item);
				gen.writeEndObject();
				gen.writeRaw(",");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
	}
}