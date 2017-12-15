package org.jboss.windup.reporting.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.syncleus.ferma.annotations.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.MapInProperties;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Holds information about a tag, as per the definition from tags.xml files.
 * The TagSetModel and TaggableModel work directly with strings for the sake of simplicity.
 * This is different from {@link TechnologyTagModel}.
 *
 * Check the current implementation to see whether or not the whole tag structure is within the graph.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 * @see TagSetModel
 * @see TaggableModel
 */
@TypeValue(TagModel.TYPE)
public interface TagModel extends WindupVertexFrame
{
    String TYPE = "TagModel";
    static final String PROP_NAME = "name";
    static final String EDGE_DESIGNATES = "designates";

    /**
     * Tag name (ID), preferably kebab-style, e.g "java-ee-6".
     */
    @Property(PROP_NAME)
    String getName();
    @Property(PROP_NAME)
    void setName(String name);

    /**
     * Human readable title of technology this tag represents, e.g "Java EE 6".
     */
    @Property("title")
    String getTitle();
    @Property("title")
    TagModel setTitle(String title);

    @JavaHandler
    String getTitleOrName();

    /**
     * A "prime" tag is one which is an important group of subtags, suitable for showing in aggregated reports.
     * For instance, "Java EE" is a good prime tag, as it may contain other technologies.
     * Whereas "frameworks" is probably not a good prime tag as it's too general.
     */
    @Property("prime")
    boolean isPrime();
    @Property("prime")
    TagModel setPrime(boolean isPrime);

    /**
     * A root tag is that which was a root in the XML definition files. These serve as entry point shortcuts when browsing the graph.
     */
    @Property("root")
    boolean isRoot();
    @Property("root")
    TagModel setRoot(boolean isRoot);

    /**
     * Pseudo tags serve as grouping for contained tags, but are not suitable to be a root tag.
     * They are also suitable for tagging related tags. In the XML files definition, such pseudo tags are often referred to by the parents="..." attribute.
     * For instance, "framework:" or "application-server:" is a suitable pseudo tag, which can demarcate tags like "wicket" or "jboss-eap".
     *
     * By convention, the names are lower case, singular, and end with a colon.
     */
    @Property("pseudo")
    boolean isPseudo();
    @Property("pseudo")
    TagModel setPseudo(boolean isPseudo);

    /**
     * A color by which this tag should typically be represented in the UI elements like tags, boxes, chart lines, graph nodes, etc.
     */
    @Property("color")
    String getColor();
    @Property("color")
    TagModel setColor(String color);

    @JavaHandler
    String toString();

    /**
     * Which tags this designates; for instance, "java-ee" designates "ejb" and "jms".
     */
    @Adjacency(label = EDGE_DESIGNATES, direction = Direction.OUT)
    Iterable<TagModel> getDesignatedTags();
    @Adjacency(label = EDGE_DESIGNATES, direction = Direction.OUT)
    TagModel setDesignatedTags(Iterable<TagModel> tags);
    @Adjacency(label = EDGE_DESIGNATES, direction = Direction.OUT)
    TagModel addDesignatedTag(TagModel tag);

    /**
     * Which tags is this tag designated by; for instance, "seam" is designated by "web" and "framework:".
     */
    @Adjacency(label = EDGE_DESIGNATES, direction = Direction.IN)
    Iterable<TagModel> getDesignatedByTags();
    @Adjacency(label = EDGE_DESIGNATES, direction = Direction.IN)
    TagModel setDesignatedByTags(Iterable<TagModel> tags);


    /**
     * Returns a map of traits - custom tag key-value pairs.
     */
    @MapInProperties(propertyPrefix = "t")
    Map<String, String> getTraits();

    /**
     * Sets a custom tag key-value pairs.
     */
    @MapInProperties(propertyPrefix = "t")
    void putTraits(String key, String value);

    /**
     * A map of traits - custom tag key-value pairs.
     */
    @MapInProperties(propertyPrefix = "t")
    void putAllTraits(Map<String, String> traits);


    public abstract class Impl implements TagModel, JavaHandlerContext<Vertex>
    {
        public String getTitleOrName()
        {
            return StringUtils.defaultString(this.getTitle(), this.getName());
        }

        @Override
        public String toString()
        {
            return "{"+this.getName()+"}";
        }
    }
}
