package org.jboss.windup.config.tags;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections4.set.UnmodifiableSet;

/**
 * Represents a tag. Determined by it's lowercased name.
 * The structure is not a tree - a tag may have multiple parents.
 *
 * @author Ondrej Zizka
 */
public final class Tag
{
    private final String name;
    /**
     * Keeps the "contains" relation.
     */
    private final Set<Tag> containedTags = new HashSet<>();
    private final Set<Tag> parentTags = new HashSet<>();
    private boolean isRoot = false;
    private boolean isPseudo = false;
    private String color = null;
    private String title = null;


    Tag(String name)
    {
        if( name == null )
            throw new IllegalArgumentException("Tag name must not be null.");
        this.name = name.toLowerCase();
    }


    public String getName()
    {
        return name;
    }


    public Set<Tag> getContainedTags()
    {
        return UnmodifiableSet.unmodifiableSet(containedTags);
    }


    public Set<Tag> getParentTags()
    {
        return UnmodifiableSet.unmodifiableSet(parentTags);
    }


    /**
     * Loops are not checked here.
     */
    public void addContainedTag(Tag tag)
    {
        this.containedTags.add(tag);
        tag.parentTags.add(this);
    }

    /**
     * Loops are not checked here.
     */
    public void addContainingTag(Tag tag)
    {
        this.parentTags.add(tag);
        tag.containedTags.add(this);
    }


    @Override
    public int hashCode()
    {
        return this.name.hashCode();
    }


    @Override
    public boolean equals(Object obj)
    {
        if( obj == null )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        final Tag other = (Tag) obj;
        if( !Objects.equals(this.name, other.name) )
            return false;
        return true;
    }


    public boolean isRoot()
    {
        return this.isRoot;
    }

    public void setIsRoot(boolean isRoot)
    {
        this.isRoot = isRoot;
    }


    public boolean isPseudo()
    {
        return isPseudo;
    }


    public void setPseudo(boolean isPseudo)
    {
        this.isPseudo = isPseudo;
    }


    public String getColor()
    {
        return color;
    }


    public void setColor(String color)
    {
        this.color = color;
    }


    public String getTitleOrName()
    {
        return title != null ? title : name;
    }

    public String getTitle()
    {
        return title;
    }


    public void setTitle(String title)
    {
        this.title = title;
    }


}
