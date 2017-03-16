package org.jboss.windup.reporting.quickfix;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class QuickfixLocationDTO
{
    private int line;
    private int column;
    private int length;

    public QuickfixLocationDTO()
    {
    }

    public QuickfixLocationDTO(int line, int column, int length)
    {
        this.line = line;
        this.column = column;
        this.length = length;
    }

    public int getLine()
    {
        return line;
    }

    public void setLine(int line)
    {
        this.line = line;
    }

    public int getColumn()
    {
        return column;
    }

    public void setColumn(int column)
    {
        this.column = column;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }
}
