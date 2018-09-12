package org.jboss.windup.rules.apps.java.reporting.rules;

import java.io.IOException;

import org.jboss.windup.reporting.model.DependencyGraphDTO;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ApplicationDependencyGraphDTOItemSerializer extends StdSerializer<DependencyGraphDTO> {

	private static final long serialVersionUID = -3443341738848344508L;
	
	private static final String KIND = "kind";
	private static final String METADATA = "metadata";
	private static final String NAME = "name";

	protected ApplicationDependencyGraphDTOItemSerializer(Class<DependencyGraphDTO> t) {
		super(t);
	}

    public ApplicationDependencyGraphDTOItemSerializer() {
        this(null);
    }

	@Override
	public void serialize(DependencyGraphDTO dto, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeString(dto.getSha1());
		gen.writeRaw(":");
		gen.writeStartObject();
		gen.writeStringField(KIND, dto.getKind());
		gen.writeObjectFieldStart(METADATA);
		gen.writeObjectField(NAME, dto.getName());
		gen.writeEndObject();
	}
}