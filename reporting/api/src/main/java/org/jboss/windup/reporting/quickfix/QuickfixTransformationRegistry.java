package org.jboss.windup.reporting.quickfix;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.services.Imported;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class QuickfixTransformationRegistry {
    @Inject
    private Imported<QuickfixTransformation> transformations;

    public QuickfixTransformation getByID(String id) {
        for (QuickfixTransformation transformation : this.transformations) {
            if (StringUtils.equals(id, transformation.getTransformationID()))
                return transformation;
        }

        return null;
    }
}
