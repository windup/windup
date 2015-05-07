package org.jboss.windup.ast.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReferenceResolvingVisitorState
{
    /**
     * Contains all wildcard imports (import com.example.*) lines from the source file.
     *
     * These are used for type resolution throughout the class.
     */
    private final List<String> wildcardImports = new ArrayList<>();

    /**
     * Indicates that we have already attempted to query the graph for this particular shortname. The shortname will
     * exist here even if no results were found.
     */
    private final Set<String> classNameLookedUp = new HashSet<>();

    /**
     * Contains a map of class short names (eg, MyClass) to qualified names (eg, com.example.MyClass)
     */
    private final Map<String, String> classNameToFQCN = new HashMap<>();
    /**
     * Maintains a set of all variable names that have been resolved
     */
    private final Set<String> names = new HashSet<String>();

    /**
     * Maintains a map of nameInstances to fully qualified class names.
     */
    private final Map<String, String> nameInstance = new HashMap<String, String>();

    public List<String> getWildcardImports()
    {
        return wildcardImports;
    }

    public Set<String> getClassNameLookedUp()
    {
        return classNameLookedUp;
    }

    public Map<String, String> getClassNameToFQCN()
    {
        return classNameToFQCN;
    }

    public Set<String> getNames()
    {
        return names;
    }

    public Map<String, String> getNameInstance()
    {
        return nameInstance;
    }
}
