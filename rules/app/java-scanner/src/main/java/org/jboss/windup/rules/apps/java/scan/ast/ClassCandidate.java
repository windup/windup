package org.jboss.windup.rules.apps.java.scan.ast;

public class ClassCandidate
{
    private final ClassCandidateType type;
    private final int lineNumber;
    private final String qualifiedName;
    private final int startPosition;
    private final int length;

    public ClassCandidate(ClassCandidateType type, int lineNumber, int startPosition, int length, String qualifiedName)
    {
        this.type = type;
        this.lineNumber = lineNumber;
        this.qualifiedName = qualifiedName;

        this.startPosition = startPosition;
        this.length = length;
    }

    public ClassCandidateType getType()
    {
        return type;
    }

    public int getStartPosition()
    {
        return startPosition;
    }

    public int getLength()
    {
        return length;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public String getQualifiedName()
    {
        return qualifiedName;
    }

    @Override
    public String toString()
    {
        return "ClassCandidate [type=" + type + ", lineNumber=" + lineNumber + ", qualifiedName="
                    + qualifiedName + ", startPosition=" + startPosition
                    + ", length=" + length + "]";
    }
}
