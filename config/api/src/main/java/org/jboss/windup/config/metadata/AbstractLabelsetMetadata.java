package org.jboss.windup.config.metadata;

import org.jboss.forge.furnace.util.Assert;

/**
 * Base class for constructing {@link LabelsetMetadata} instances. Provides sensible defaults.
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
public class AbstractLabelsetMetadata implements LabelProviderMetadata {
    private final String id;

    /**
     * Construct a new {@link AbstractLabelsetMetadata} instance using the given {@link String} ID.
     */
    public AbstractLabelsetMetadata(String id) {
        Assert.notNull(id, "Labelset ID must not be null.");
        this.id = id;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getOrigin() {
        return getClass().getClassLoader().toString();
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractLabelsetMetadata other = (AbstractLabelsetMetadata) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LabelsetMetadata ["
                + "\tid=" + id + ", "
                + "\tdescription=" + getDescription() + ", "
                + "\torigin=" + getOrigin() + ""
                + "]";
    }

}
