package org.jboss.windup.config.metadata;

/**
 * Create concrete classes of {@Link LabelProviderMetadata}
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
public class LabelMetadataBuilder extends AbstractLabelsetMetadata implements LabelProviderMetadata
{
    private String origin;
    private String description;
    private int priority;

    /**
     * When no priority is specified, then we assign it. Read more about priority here {@link LabelsetMetadata#getPriority()}
     */
    public LabelMetadataBuilder(String ID, String description)
    {
        super(ID);
        this.description = description;
        this.priority = Integer.MAX_VALUE;
    }

    public LabelMetadataBuilder(String ID, String description, int priority)
    {
        this(ID, description);
        this.priority = priority;
    }

    @Override
    public String getOrigin()
    {
        return origin == null ? super.getOrigin() : origin;
    }

    public void setOrigin(String origin)
    {
        this.origin = origin;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public int getPriority()
    {
        return priority;
    }
}
