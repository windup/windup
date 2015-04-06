package com.tinkerpop.frames.domain.incidences;

import com.tinkerpop.frames.EdgeFrame;
import com.tinkerpop.frames.InVertex;
import com.tinkerpop.frames.OutVertex;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.domain.classes.Person;
import com.tinkerpop.frames.domain.classes.Project;

/**
 * This class frames "created" edges (just like Created and CreatedBy), but uses
 * the {@link InVertex} and {@link OutVertex} annotations instead of the now deprecated
 * Source/Target.
 */
public interface CreatedInfo extends EdgeFrame {
	@OutVertex
	Person getPerson();

	@InVertex
	Project getProject();

	@Property("weight")
	public Float getWeight();

	@Property("weight")
	public void setWeight(float weight);
}
