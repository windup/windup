package org.jboss.windup.ast.java.data;

/**
 * Contains a name that has been referenced by the Java source file. This can include the qualified name (for example, com.example.data.Foo) as well
 * as information about the reference. Information includes indicating where the reference was found within the file (line, column, and length) as
 * well as how it was used (import, method call, etc).
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class ClassReference
{
    private final String qualifiedName;
    private final int lineNumber;
    private final int column;
    private final int length;
    private final TypeReferenceLocation location;
    private String line;

    /**
     * Creates the {@link ClassReference} with the given qualfiedName, location, lineNumber, column, and length.
     */
    public ClassReference(String qualifiedName, TypeReferenceLocation location, int lineNumber, int column, int length, String line)
    {
        this.qualifiedName = qualifiedName;
        this.location = location;
        this.lineNumber = lineNumber;
        this.column = column;
        this.length = length;
        this.line= line;
    }

    /**
     * Contains the raw text represented by this reference (class names are not resolved).
     */
    public String getLine()
    {
        return line;
    }

    /**
     * Contains the raw text represented by this reference (class names are not resolved).
     */
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
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
        ClassReference other = (ClassReference) obj;
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
        return "ClassReference [qualifiedName=" + qualifiedName + ", lineNumber=" + lineNumber + ", column=" + column + ", length=" + length
                    + ", location=" + location + ", line=" + line + "]";
    }
}
