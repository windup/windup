package org.jboss.windup.ast.java;

import java.util.List;

public interface WildcardImportResolver
{
    String resolve(List<String> wildcardImports, String name);

    String[] resolve(String wildcardImportPackageName);
}
