package org.jboss.windup.ast.java;

import java.util.List;

/**
 * Provides a pluggable lookup mechanism for resolving wildcard imports.
 */
public interface WildcardImportResolver
{
    /**
     * Resolve the given name based upon the provided wildcard imports.
     */
    String resolve(List<String> wildcardImports, String name);

    /**
     * Find all potential imports for this wildcard package name.
     */
    String[] resolve(String wildcardImportPackageName);
}
