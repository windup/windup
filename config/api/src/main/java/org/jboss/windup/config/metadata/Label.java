package org.jboss.windup.config.metadata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the content of a Label. In case the label is inside an XML file, then this
 * class should load the information inside the tag <label id="id">...<label>. E.g.:
 * <labelset>
 *     <labels>
 *         <label id="id">
 *             <name>name</name>
 *             <description>description</description>
 *             <supported>
 *                 <tag>tag</tag>
 *             </supported>
 *             <unsuitable>
 *                 <tag>tag</tag>
 *             </unsuitable>
 *             <neutral>
 *                 <tag>tag</tag>
 *             </neutral>
 *         </label>
 *     </labels>
 * </labelset>
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
public class Label
{
    private final String id;
    private final String name;
    private final String description;
    private final Set<String> supported = new HashSet<>();
    private final Set<String> unsuitable = new HashSet<>();
    private final Set<String> neutral = new HashSet<>();

    /**
     * The String representation of a label. If label is inside an XML file, then this field should keep the
     * value of the XML tag <label id="id">...<label> and all its content.
     * This is used by windup-web when rendering the content of a Label.
     */
    private String labelString;

    public Label(String id, String name)
    {
        this.id = id;
        this.name = name;
        this.description = null;
    }

    public Label(String id, String name, String description)
    {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Label(String id, String name, String description, String labelString)
    {
        this(id, name, description);
        this.labelString = labelString;
    }

    public void addSupportedTags(Collection<String> tags)
    {
        this.supported.addAll(tags);
    }

    public void addUnsuitableTags(Collection<String> tags)
    {
        this.unsuitable.addAll(tags);
    }

    public void addNeutralTags(Collection<String> tags)
    {
        this.neutral.addAll(tags);
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public Set<String> getSupported()
    {
        return supported;
    }

    public Set<String> getUnsuitable()
    {
        return unsuitable;
    }

    public Set<String> getNeutral()
    {
        return neutral;
    }

    public String getId()
    {
        return id;
    }

    public String getLabelString()
    {
        return labelString;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Label label = (Label) o;
        return name.equals(label.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
