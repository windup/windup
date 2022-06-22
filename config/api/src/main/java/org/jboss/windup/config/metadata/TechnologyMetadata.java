package org.jboss.windup.config.metadata;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public abstract class TechnologyMetadata {
    private final TechnologyReference technology;

    public TechnologyMetadata(TechnologyReference technology) {
        this.technology = technology;
    }

    public TechnologyReference getTechnology() {
        return technology;
    }

    public abstract boolean handles(TechnologyReference technology);
}
