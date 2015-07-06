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
    private final ResolutionStatus resolutionStatus;
    private final int lineNumber;
    private final int column;
    private final int length;
    private final TypeReferenceLocation location;
    private String line;

    /**
     * Creates the {@link ClassReference} with the given qualfiedName, location, lineNumber, column, and length.
     */
    public ClassReference(String qualifiedName, ResolutionStatus resolutionStatus, TypeReferenceLocation location, int lineNumber,
                int column, int length, String line)
    {
        this.qualifiedName = qualifiedName;
        this.resolutionStatus = resolutionStatus;
        this.location = location;
        this.lineNumber = lineNumber;
        this.column = column;
        this.length = length;
        this.line = line;
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

    public ResolutionStatus getResolutionStatus()
    {
        return resolutionStatus;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ClassReference that = (ClassReference) o;

        if (lineNumber != that.lineNumber)
            return false;
        if (column != that.column)
            return false;
        if (length != that.length)
            return false;
        if (qualifiedName != null ? !qualifiedName.equals(that.qualifiedName) : that.qualifiedName != null)
            return false;
        if (resolutionStatus != that.resolutionStatus)
            return false;
        if (location != that.location)
            return false;
        return !(line != null ? !line.equals(that.line) : that.line != null);
    }

    @Override
    public int hashCode()
    {
        int result = qualifiedName != null ? qualifiedName.hashCode() : 0;
        result = 31 * result + (resolutionStatus != null ? resolutionStatus.hashCode() : 0);
        result = 31 * result + lineNumber;
        result = 31 * result + column;
        result = 31 * result + length;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (line != null ? line.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "ClassReference [qualifiedName=" + qualifiedName + ", resolve status=" + resolutionStatus + ", lineNumber=" + lineNumber + ", column="
                    + column + ", length=" + length
                    + ", location=" + location + ", line=" + line + "]";
    }
}
