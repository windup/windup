package org.jboss.windup.ast.java.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains a list of {@link JavaClassReference}s, which themselves describe the contents of the Java source file, and all of the things that it
 * directly references.
 * 
 * @author jsightler
 *
 */
public class JavaClassReferences
{
    private List<JavaClassReference> references = new ArrayList<>();

    /**
     * Adds a {@link JavaClassReference}
     */
    public void addReference(JavaClassReference reference)
    {
        this.references.add(reference);
    }

    /**
     * Gets the list of all {@link JavaClassReference}s
     */
    public List<JavaClassReference> getReferences()
    {
        return Collections.unmodifiableList(this.references);
    }
}
