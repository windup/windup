package org.jboss.windup.tooling.rules;

import java.util.HashSet;
import java.util.Set;

public class TagImpl implements Tag
{
	private static final long serialVersionUID = 1L;
	public static final String TAG_ID = "tag_id";
    public static final String TAG_NAME = "name";

    private Long id;

    private String name;
    private boolean isRoot;
    private boolean isPseudo;
    private String color;
    private String title;
    private Set<Tag> containedTags;
    private Tag parent;

    protected TagImpl()
    {
        this.containedTags = new HashSet<>();
    }

    public TagImpl(String name)
    {
        this();
        this.name = name;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public Tag setName(String name)
    {
        this.name = name;

        return this;
    }

    public boolean isRoot()
    {
        return isRoot;
    }

    public Tag setRoot(boolean root)
    {
        isRoot = root;

        return this;
    }

    public boolean isPseudo()
    {
        return isPseudo;
    }

    public Tag setPseudo(boolean pseudo)
    {
        isPseudo = pseudo;

        return this;
    }

    public String getColor()
    {
        return color;
    }

    public Tag setColor(String color)
    {
        this.color = color;

        return this;
    }

    public String getTitle()
    {
        return title;
    }

    public Tag setTitle(String title)
    {
        this.title = title;

        return this;
    }

    public Set<Tag> getContainedTags()
    {
        return containedTags;
    }

    public Tag setContainedTags(Set<Tag> containedTags)
    {
        this.containedTags = containedTags;

        return this;
    }

    public Tag addContainedTag(Tag tag)
    {
        this.containedTags.add(tag);

        return this;
    }

    public Tag getParent()
    {
        return parent;
    }

    public Tag setParent(Tag parent)
    {
        this.parent = parent;

        return this;
    }
}
