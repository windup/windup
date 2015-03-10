package org.jboss.windup.ast.java.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains a name that has been referenced by the Java source file. This can include the qualified name (for example, com.example.data.Foo) as well
 * as information about the reference. Information includes indicating where the reference was found within the file (line, column, and length) as
 * well as how it was used (import, method call, etc).
 * 
 * @author jsightler
 *
 */
public class JavaClassReference
{
    private final String qualifiedName;
    private final int lineNumber;
    private final int column;
    private final int length;
    private Map<String, String> annotationValues = new HashMap<>();
    private final TypeReferenceLocation location;
    private String line;

    /**
     * Creates the {@link JavaClassReference} with the given qualfiedName, location, lineNumber, column, and length.
     */
    public JavaClassReference(String qualifiedName, TypeReferenceLocation location, int lineNumber, int column, int length, String line)
    {
        this.qualifiedName = qualifiedName;
        this.location = location;
        this.lineNumber = lineNumber;
        this.column = column;
        this.length = length;
        this.line= line;
    }

    public String getLine()
    {
        return line;
    }

    public void setLine(String line)
    {
        this.line = line;
    }

    /**
     * Gets the fully qualified name of the Java element that was referenced.
     */
    public String getQualifiedName()
    {
        return qualifiedName;
    }

    /**
     * Gets the line number where this reference was located.
     */
    public int getLineNumber()
    {
        return lineNumber;
    }

    /**
     * Gets the column where this reference was located.
     */
    public int getColumn()
    {
        return column;
    }

    /**
     * Gets the length of the reference.
     */
    public int getLength()
    {
        return length;
    }

    /**
     * The {@link TypeReferenceLocation} indicates how this reference was used within the file. For example, this can indicate whether it was used as
     * an annotation or a method call.
     */
    public TypeReferenceLocation getLocation()
    {
        return location;
    }

    /**
     * If the item found is the use of an Annotation, then this will contain a map with the values used by the annotation.
     * 
     * Nested values are not currently supported here.
     */
    public void setAnnotationValues(Map<String, String> annotationValues)
    {
        this.annotationValues = annotationValues;
    }

    /**
     * If the item found is the use of an Annotation, then this will contain a map with the values used by the annotation.
     * 
     * Nested values are not currently supported here.
     */
    public Map<String, String> getAnnotationValues()
    {
        return Collections.unmodifiableMap(annotationValues);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((annotationValues == null) ? 0 : annotationValues.hashCode());
        result = prime * result + column;
        result = prime * result + length;
        result = prime * result + lineNumber;
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((qualifiedName == null) ? 0 : qualifiedName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JavaClassReference other = (JavaClassReference) obj;
        if (annotationValues == null)
        {
            if (other.annotationValues != null)
                return false;
        }
        else if (!annotationValues.equals(other.annotationValues))
            return false;
        if (column != other.column)
            return false;
        if (length != other.length)
            return false;
        if (lineNumber != other.lineNumber)
            return false;
        if(line == null) {
            if(other.line!=null) {
                return false;
            }
        } else {
            if(!line.equals(other.line)) {
                return false;
            }
        }
        if (location != other.location)
            return false;
        if (qualifiedName == null)
        {
            if (other.qualifiedName != null)
                return false;
        }
        else if (!qualifiedName.equals(other.qualifiedName))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "JavaClassReference [qualifiedName=" + qualifiedName + ", lineNumber=" + lineNumber + ", column=" + column + ", length=" + length
                    + ", annotationValues=" + annotationValues + ", location=" + location +  ", line=" + line + "]";
    }
}
