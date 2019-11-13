package org.jboss.windup.config.metadata;

public class LabelMetadataBuilder extends AbstractLabelsetMetadata implements LabelProviderMetadata
{
    private String origin;
    private String description;
    private int priority;

    public LabelMetadataBuilder(String ID, String description) {
        super(ID);
        this.description = description;
        this.priority = Integer.MAX_VALUE;
    }

    public LabelMetadataBuilder(String ID, String description, int priority) {
        super(ID);
        this.priority = priority;
        this.description = description;
    }

    @Override
    public String getOrigin()
    {
        return origin == null ? super.getOrigin() : origin;
    }

    public LabelMetadataBuilder setOrigin(String origin)
    {
        this.origin = origin;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
