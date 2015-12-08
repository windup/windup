package org.jboss.windup.config.tags;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections4.set.UnmodifiableSet;

/**
 * Represents a tag. Determined by it's lowercased name.
 */
public final class Tag
{
    private final String name;
    /**
     * Keeps the "contains" relation.
     */
    private final Set<Tag> containedTags = new HashSet<>();
    private final Set<Tag> containingTags = new HashSet<>();
    private boolean isRoot = false;


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
        return UnmodifiableSet.unmodifiableSet(containingTags);
    }


    /**
     * Loops are not checked here.
     */
    public void addContainedTag(Tag tag)
    {
        this.containedTags.add(tag);
        tag.containingTags.add(this);
    }

    /**
     * Loops are not checked here.
     */
    public void addContainingTag(Tag tag)
    {
        this.containingTags.add(tag);
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


    public void setRoot(boolean isRoot)
    {
        this.isRoot = isRoot;
    }

    public boolean isRoot()
    {
        return this.isRoot;
    }
}
