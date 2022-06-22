package org.jboss.windup.ast.java;

import java.util.List;

/**
 * Provides a default implementation of {@link WildcardImportResolver} resolver that does no actual resolution. This is mostly useful in tests or in
 * cases where it is known that the JDT will be able to resolve all names.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class NoopWildcardImportResolver implements WildcardImportResolver {

    @Override
    public String resolve(List<String> wildcardImports, String name) {
        return null;
    }

    @Override
    public String[] resolve(String wildcardImportPackageName) {
        return new String[0];
    }

}
