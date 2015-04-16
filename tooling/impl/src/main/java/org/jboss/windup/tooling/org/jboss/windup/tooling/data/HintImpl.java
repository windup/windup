package org.jboss.windup.tooling.org.jboss.windup.tooling.data;

import java.io.File;
import java.util.List;

import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.tooling.data.Hint;
import org.jboss.windup.tooling.data.Link;

/**
 * This is equivalent to a {@link InlineHintModel}, however it contains no dependencies on having an open instance of the graph in order to operate.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class HintImpl implements Hint
{
    private File file;
    private String title;
    private String hint;
    private Severity severity;
    private int effort;
    private List<Link> links;

    private int lineNumber;
    private int column;
    private int length;
    private String sourceSnippit;
    private String ruleID;

    /**
     * This references the {@link File} referenced by this {@link Hint}.
     */
    @Override
    public File getFile()
    {
        return file;
    }

    /**
     * This references the {@link File} referenced by this {@link Hint}.
     */
    public void setFile(File file)
    {
        this.file = file;
    }

    /**
     * Contains a title for this hint. This should describe the problem itself (for example, "Usage of proprietary class: Foo")
     */
    @Override
    public String getTitle()
    {
        return title;
    }

    /**
     * Contains a title for this hint. This should describe the problem itself (for example, "Usage of proprietary class: Foo")
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * This contains descriptive text describing the problem and how the problem can be solved.
     */
    @Override
    public String getHint()
    {
        return hint;
    }

    /**
     * This contains descriptive text describing the problem and how the problem can be solved.
     */
    public void setHint(String hint)
    {
        this.hint = hint;
    }

    /**
     * This is a hint as to the severity of the problem. This may be used for supplying an icon or glyph in the report to the user.
     */
    @Override
    public Severity getSeverity()
    {
        return severity;
    }

    /**
     * This is a hint as to the severity of the problem. This may be used for supplying an icon or glyph in the report to the user.
     */
    public void setSeverity(Severity severity)
    {
        this.severity = severity;
    }

    /**
     * This contains the effort level as an integer (Story Points). This is based on the Scrum "modified-Fibonacci" system of effort estimation.
     */
    @Override
    public int getEffort()
    {
        return effort;
    }

    /**
     * This contains the effort level as an integer (Story Points). This is based on the Scrum "modified-Fibonacci" system of effort estimation.
     */
    public void setEffort(int effort)
    {
        this.effort = effort;
    }

    /**
     * This contains a list of {@link Link}s for further information about the problem and its solution.
     */
    @Override
    public List<Link> getLinks()
    {
        return links;
    }

    /**
     * This contains a list of {@link Link}s for further information about the problem and its solution.
     */
    public void setLinks(List<Link> links)
    {
        this.links = links;
    }

    /**
     * This contains the line number of the problem.
     */
    @Override
    public int getLineNumber()
    {
        return lineNumber;
    }

    /**
     * This contains the line number of the problem.
     */
    public void setLineNumber(int lineNumber)
    {
        this.lineNumber = lineNumber;
    }

    /**
     * This contains the column number within that line.
     */
    @Override
    public int getColumn()
    {
        return column;
    }

    /**
     * This contains the column number within that line.
     */
    public void setColumn(int column)
    {
        this.column = column;
    }

    /**
     * This contains the length of the code section being referenced. For example, if the original code was "f.a()", this would be be "5".
     */
    @Override
    public int getLength()
    {
        return length;
    }

    /**
     * This contains the length of the code section being referenced. For example, if the original code was "f.a()", this would be be "5".
     */
    public void setLength(int length)
    {
        this.length = length;
    }

    /**
     * This contains the original source code itself (for example, "proprietaryobject.doStuff()").
     */
    @Override
    public String getSourceSnippit()
    {
        return sourceSnippit;
    }

    /**
     * This contains the original source code itself (for example, "proprietaryobject.doStuff()").
     */
    public void setSourceSnippit(String sourceSnippit)
    {
        this.sourceSnippit = sourceSnippit;
    }

    /**
     * This contains the id of the rule that produced this {@link Hint}.
     */
    @Override
    public String getRuleID()
    {
        return ruleID;
    }

    /**
     * This contains the id of the rule that produced this {@link Hint}.
     */
    public void setRuleID(String ruleID)
    {
        this.ruleID = ruleID;
    }
}
