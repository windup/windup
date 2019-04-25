package org.jboss.windup.config.metadata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
public class Label
{
    private final String id;
    private final String name;
    private final String description;
    private Set<String> supported = new HashSet<>();
    private Set<String> unsuitable = new HashSet<>();
    private Set<String> neutral = new HashSet<>();

    public Label(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public void addSupportedTags(Collection<String> tags) {
        this.supported.addAll(tags);
    }

    public void addUnsuitableTags(Collection<String> tags) {
        this.unsuitable.addAll(tags);
    }

    public void addNeutralTags(Collection<String> tags) {
        this.neutral.addAll(tags);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getSupported() {
        return supported;
    }

    public Set<String> getUnsuitable() {
        return unsuitable;
    }

    public Set<String> getNeutral() {
        return neutral;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label = (Label) o;
        return name.equals(label.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
