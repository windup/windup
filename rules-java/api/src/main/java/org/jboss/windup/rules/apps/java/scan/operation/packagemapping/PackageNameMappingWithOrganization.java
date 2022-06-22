package org.jboss.windup.rules.apps.java.scan.operation.packagemapping;

import org.ocpsoft.rewrite.config.Rule;

/**
 * Indicates that everything has been set, except for the ID, which is optional.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface PackageNameMappingWithOrganization extends Rule {
    /**
     * Assigns the given ID to the Rule.
     */
    Rule withId(String id);
}
