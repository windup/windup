package org.jboss.windup.config.tags;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a tag. Determined by it's lowercased name. The structure is not a tree - a tag may have multiple "parents".
 * <p>
 * Note that the "parent" and "contained tags" is misleading. The tags structure is in fact an oriented acyclic graph. (It could even be cyclic if we
 * allowed for synonyms.) Better names would be "designated tags" and "designated by tags" or such.
 *
 * @author Ondrej Zizka
 */
public final class Tag {
    private final String name;
    /**
     * Keeps the "contains" relation.
     */
    private final Set<Tag> containedTags = new HashSet<>();
    private final Set<Tag> parentTags = new HashSet<>();
    private boolean isPrime = false;
    private boolean isPseudo = false;
    private boolean isRoot = false;
    private String color = null;
    private String title = null;
    private Map<String, String> traits = null; // Not needed in most cases.

    Tag(String name) {
        this.name = normalizeName(name);
    }

    public static String normalizeName(String name) {
        if (name == null)
            throw new IllegalArgumentException("Tag name must not be null.");
        return name.toLowerCase().replace(' ', '-');
    }

    /**
     * Tag name (ID), preferably kebab-style, e.g "java-ee-6".
     */
    public String getName() {
        return name;
    }

    /**
     * Which tags are designated by this tag; for instance, "java-ee" designates "ejb" and "jms".
     */
    public Set<Tag> getContainedTags() {
        return Collections.unmodifiableSet(containedTags);
    }

    /**
     * Which tags are designated by this tag; for instance, "seam" is designated by "web" and "framework:".
     */
    public Set<Tag> getParentTags() {

        return Collections.unmodifiableSet(parentTags);
    }

    /**
     * Loops are not checked here.
     */
    public void addContainedTag(Tag tag) {
        this.containedTags.add(tag);
        tag.parentTags.add(this);
    }

    /**
     * Loops are not checked here.
     */
    public void addContainingTag(Tag tag) {
        this.parentTags.add(tag);
        tag.containedTags.add(this);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Tag other = (Tag) obj;
        if (!Objects.equals(this.name, other.name))
            return false;
        return true;
    }

    /**
     * A root tag is that which was a root in the XML definition files. These serve as entry point shortcuts when browsing the graph.
     */
    public boolean isRoot() {
        return this.isRoot;
    }

    public void setIsRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    /**
     * A "prime" tag is one which is an important group of subtags, suitable for showing in aggregated reports. For instance, "Java EE" is a good
     * prime tag, as it may contain other technologies. Whereas "frameworks" is probably not a good prime tag as it's too general.
     */
    public boolean isPrime() {
        return this.isPrime;
    }

    public void setIsPrime(boolean isPrime) {
        this.isPrime = isPrime;
    }

    /**
     * Pseudo tags serve as grouping for contained tags, but are not suitable to be a root tag. They are also suitable for tagging related tags. In
     * the XML files definition, such pseudo tags are often referred to by the parents="..." attribute. For instance, "framework:" or
     * "application-server:" is a suitable pseudo tag, which can demarcate tags like "wicket" or "jboss-eap".
     * <p>
     * By convention, the names are lower case, singular, and end with a colon.
     */
    public boolean isPseudo() {
        return isPseudo;
    }

    public void setPseudo(boolean isPseudo) {
        this.isPseudo = isPseudo;
    }

    /**
     * A color by which this tag should typically be represented in the UI elements like tags, boxes, chart lines, graph nodes, etc.
     */
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Human readable title of technology this tag represents, e.g "Java EE 6".
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleOrName() {
        return title != null ? title : name;
    }

    /**
     * Returns the traits map, or null if it was not yet initialized.
     */
    public Map<String, String> getTraits() {
        return traits;
    }

    public Map<String, String> getOrCreateTraits() {
        return traits != null ? traits : (traits = new HashMap<>());
    }

    @Override
    public String toString() {
        return "#" + name + "(" + (containedTags == null ? '-' : containedTags.size()) + ')';
    }

}
