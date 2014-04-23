package org.jboss.windup.engine.visitor.inspector.decompiler;

public class ClassCandidate
{

    private final int lineNumber;
    private final String qualifiedName;
    private final int startPosition;
    private final int length;

    public ClassCandidate(int lineNumber, int startPosition, int length, String qualifiedName)
    {
        this.lineNumber = lineNumber;
        this.qualifiedName = qualifiedName;

        this.startPosition = startPosition;
        this.length = length;
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
        return "ClassCandidate [lineNumber=" + lineNumber + ", qualifiedName="
                    + qualifiedName + ", startPosition=" + startPosition
                    + ", length=" + length + "]";
    }
}
