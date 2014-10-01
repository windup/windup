package com.tinkerpop.frames.domain.incidences;

import com.tinkerpop.frames.Domain;
import com.tinkerpop.frames.EdgeFrame;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.Range;
import com.tinkerpop.frames.domain.classes.Person;
import com.tinkerpop.frames.domain.classes.Project;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Created extends EdgeFrame {
	//Please note: Domain and Range are incorrectly annotated here, they should be inverted if you want this interface working
	// with @Incidence annotations. See FramedEdgeTest.testGettingDomainAndRange.
	
    @Domain
    public Project getDomain();

    @Range
    public Person getRange();

    @Property("weight")
    public Float getWeight();

    @Property("weight")
    public void setWeight(float weight);
}
