package com.tinkerpop.frames.domain.classes;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Incidence;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.domain.incidences.CreatedBy;
import com.tinkerpop.frames.domain.incidences.CreatedInfo;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerClass;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@JavaHandlerClass(ProjectImpl.class)
public interface Project extends NamedObject {

    @Property("lang")
    public String getLanguage();

    @Adjacency(label = "created", direction = Direction.IN)
    public Iterable<Person> getCreatedByPeople();

    @Incidence(label = "created", direction = Direction.IN)
    public Iterable<CreatedBy> getCreatedBy();
    
    @Incidence(label = "created", direction = Direction.IN)
    public Iterable<CreatedInfo> getCreatedInfo();

    @Adjacency(label = "created", direction = Direction.IN)
    public void removeCreatedByPerson(Person person);

    @Incidence(label = "created", direction = Direction.IN)
    public void removeCreatedBy(CreatedBy createdBy);
    
    @Incidence(label = "created", direction = Direction.IN)
    public void removeCreatedInfo(CreatedInfo createdBy);
    
    @Incidence(label = "created", direction = Direction.IN)
    public CreatedBy addCreatedByPersonIncidence(Person person);
    
    @Incidence(label = "created", direction = Direction.IN)
    public CreatedInfo addCreatedByPersonInfo(Person person);
    
    @Incidence(label = "created", direction = Direction.IN)
    public CreatedBy addCreatedInfo(CreatedInfo person);

    @Adjacency(label = "created", direction = Direction.IN)
    public void addCreatedByPersonAdjacency(Person person);
    
    @JavaHandler
    public String getLanguageUsingMixin();
    
}

