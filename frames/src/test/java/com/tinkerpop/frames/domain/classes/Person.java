package com.tinkerpop.frames.domain.classes;

import java.util.Map;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Incidence;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import com.tinkerpop.frames.domain.incidences.Created;
import com.tinkerpop.frames.domain.incidences.CreatedInfo;
import com.tinkerpop.frames.domain.incidences.Knows;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Person extends NamedObject {
    enum Gender {FEMALE, MALE};

    @Property("age")
    public Integer getAge();

    @Property("age")
    public void setAge(Integer age);

    @Property("age")
    public void removeAge();

    @Property("gender")
    public void setGender(Gender gender);

    @Property("gender")
    public Gender getGender();

    @Property("gender")
    public void removeGender();

    @Incidence(label = "knows")
    public Iterable<Knows> getKnows();

    @Adjacency(label = "knows")
    public Iterable<Person> getKnowsPeople();

    @Adjacency(label = "knows")
    public void setKnowsPeople(final Iterable<Person> knows);

    @Incidence(label = "created")
    public Iterable<Created> getCreated();
    
    @Incidence(label = "created")
    public Iterable<CreatedInfo> getCreatedInfo();

    @Adjacency(label = "created")
    public Iterable<Project> getCreatedProjects();

    @Adjacency(label = "knows")
    public void addKnowsPerson(final Person person);

    @Adjacency(label = "knows")
    public Person addKnowsNewPerson();

    @Incidence(label = "knows")
    public Knows addKnows(final Person person);

    @Adjacency(label = "created")
    public void addCreatedProject(final Project project);

    @Incidence(label = "created")
    public Created addCreated(final Project project);
    
    @Incidence(label = "created")
    public CreatedInfo addCreatedInfo(final Project project);

    @Adjacency(label = "knows")
    public void removeKnowsPerson(final Person person);

    @Incidence(label = "knows")
    public void removeKnows(final Knows knows);

    @Adjacency(label = "latestProject")
    public Project getLatestProject();

    @Adjacency(label = "latestProject")
    public void setLatestProject(final Project latestProject);

    @GremlinGroovy("it.as('x').out('created').in('created').except('x')")
    public Iterable<Person> getCoCreators();

    @GremlinGroovy("_().as('x').out('created').in('created').except('x').shuffle")
    public Person getRandomCoCreators();

    @GremlinGroovy("_().as('x').out('created').in('created').except('x').has('age',age)")
    public Person getCoCreatorOfAge(@GremlinParam("age") int age);

    @GremlinGroovy(value = "'aStringProperty'", frame = false)
    public String getAStringProperty();

    @GremlinGroovy(value = "['a','b','c']", frame = false)
    public Iterable<String> getListOfStrings();

    @GremlinGroovy("it.as('x').out('created').in('created').except('x').groupCount.cap.next()")
    public Map<Person, Long> getRankedCoauthors();
    
    @GremlinGroovy("person.asVertex().in('knows')")
    public Iterable<Person> getKnownRootedFromParam(@GremlinParam("person") Person person);

    @Deprecated
    @GremlinGroovy("_().out('knows')")
    public Iterable<Person> getDeprecatedKnowsPeople();

    @Property("boolean")
    public void setBoolean(boolean b);

    @Property("boolean")
    public boolean isBooleanPrimitive();

    @Property("boolean")
    public Boolean isBoolean();

    @Property("boolean")
    public boolean canBooleanPrimitive();

    @Property("boolean")
    public Boolean canBoolean();
    
    @Adjacency(label = "knows", direction=Direction.BOTH)
    public void addKnowsPersonDirectionBothError(final Person person);
    
    @Adjacency(label = "knows", direction=Direction.BOTH)
    public void setKnowsPersonDirectionBothError(final Person person);
    
    @Incidence(label = "created", direction=Direction.BOTH)
    public Created addCreatedDirectionBothError(final Project project);
    
    @JavaHandler
    public String getNameAndAge();
    
    @JavaHandler
    public Iterable<Person> getCoCreatorsJava();
    
    @JavaHandler
    public void notImplemented();
    
    
    abstract class Impl implements JavaHandlerContext<Vertex>, Person {

    	@Override
    	@JavaHandler
    	public String getNameAndAge() {
    		return getName() + " (" + getAge() + ")";
    	}
    	
    	@Override
    	@JavaHandler
    	public Iterable<Person> getCoCreatorsJava() {
    		return frameVertices(gremlin().as("x").out("created").in("created").except("x"));
    		
    	}
    }


	public void unhandledNoAnnotation();

	@GremlinGroovy("")
	public void unhandledNoHandler();
	
	

}
