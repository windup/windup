package org.jboss.windup.rules.apps.java.scan.operation.packagemapping;

import org.ocpsoft.rewrite.config.Rule;

/**
 * Interface to assist in the construction of {@link PackageNameMapping} {@link Rule}s.
 */
public interface PackageNameMappingWithPackagePattern
{
    /**
     * Sets the package pattern to match against.
     */
    Rule toOrganization(String organization);
}
