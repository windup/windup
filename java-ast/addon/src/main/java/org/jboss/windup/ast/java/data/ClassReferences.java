package org.jboss.windup.ast.java.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains a list of {@link ClassReference}s, which themselves describe the contents of the Java source file, and all of the things that it
 * directly references.
 * 
 * @author jsightler
 *
 */
public class ClassReferences
{
    private List<ClassReference> references = new ArrayList<>();

    /**
     * Adds a {@link ClassReference}
     */
    public void addReference(ClassReference reference)
    {
        this.references.add(reference);
    }

    /**
     * Gets the list of all {@link ClassReference}s
     */
    public List<ClassReference> getReferences()
    {
        return Collections.unmodifiableList(this.references);
    }
}
