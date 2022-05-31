package org.jboss.windup.reporting.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.MapInProperties;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.List;
import java.util.Map;

/**
 * Holds information about a tag, as per the definition from tags.xml files.
 * The TagSetModel and TaggableModel work directly with strings for the sake of simplicity.
 * This is different from {@link TechnologyTagModel}.
 * <p>
 * Check the current implementation to see whether or not the whole tag structure is within the graph.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 * @see TagSetModel
 * @see TaggableModel
 */
@TypeValue(TagModel.TYPE)
public interface TagModel extends WindupVertexFrame {
    String TYPE = "TagModel";
    String PROP_NAME = "name";
    String EDGE_DESIGNATES = "designates";

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
    void setTitle(String title);

    default String getTitleOrName() {
        return StringUtils.defaultString(this.getTitle(), this.getName());
    }

    /**
     * A "prime" tag is one which is an important group of subtags, suitable for showing in aggregated reports.
     * For instance, "Java EE" is a good prime tag, as it may contain other technologies.
     * Whereas "frameworks" is probably not a good prime tag as it's too general.
     */
    @Property("prime")
    boolean isPrime();

    @Property("prime")
    void setPrime(boolean isPrime);

    /**
     * A root tag is that which was a root in the XML definition files. These serve as entry point shortcuts when browsing the graph.
     */
    @Property("root")
    boolean isRoot();

    @Property("root")
    void setRoot(boolean isRoot);

    /**
     * Pseudo tags serve as grouping for contained tags, but are not suitable to be a root tag.
     * They are also suitable for tagging related tags. In the XML files definition, such pseudo tags are often referred to by the parents="..." attribute.
     * For instance, "framework:" or "application-server:" is a suitable pseudo tag, which can demarcate tags like "wicket" or "jboss-eap".
     * <p>
     * By convention, the names are lower case, singular, and end with a colon.
     */
    @Property("pseudo")
    boolean isPseudo();

    @Property("pseudo")
    void setPseudo(boolean isPseudo);

    /**
     * A color by which this tag should typically be represented in the UI elements like tags, boxes, chart lines, graph nodes, etc.
     */
    @Property("color")
    String getColor();

    @Property("color")
    void setColor(String color);

    /**
     * Which tags this designates; for instance, "java-ee" designates "ejb" and "jms".
     */
    @Adjacency(label = EDGE_DESIGNATES, direction = Direction.OUT)
    List<TagModel> getDesignatedTags();

    @Adjacency(label = EDGE_DESIGNATES, direction = Direction.OUT)
    void setDesignatedTags(List<TagModel> tags);

    @Adjacency(label = EDGE_DESIGNATES, direction = Direction.OUT)
    void addDesignatedTag(TagModel tag);

    /**
     * Which tags is this tag designated by; for instance, "seam" is designated by "web" and "framework:".
     */
    @Adjacency(label = EDGE_DESIGNATES, direction = Direction.IN)
    List<TagModel> getDesignatedByTags();

    @Adjacency(label = EDGE_DESIGNATES, direction = Direction.IN)
    void setDesignatedByTags(List<TagModel> tags);


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

}
