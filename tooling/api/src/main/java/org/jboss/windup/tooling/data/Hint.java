package org.jboss.windup.tooling.data;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * This is equivalent to a {@link InlineHintModel}, however it contains no dependencies on having an open instance of the graph in order to operate.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface Hint extends Serializable {
    /**
     * Contains a unique identifier for this hint.
     */
    Object getID();

    /**
     * This references the {@link File} referenced by this {@link Hint}.
     */
    File getFile();

    /**
     * Contains a title for this hint. This should describe the problem itself (for example, "Usage of proprietary class: Foo")
     */
    String getTitle();

    /**
     * This contains descriptive text describing the problem and how the problem can be solved.
     */
    String getHint();

    /**
     * This is a hint as to the severity of the problem. This may be used for supplying an icon or glyph in the report to the user.
     */
    IssueCategory getIssueCategory();

    /**
     * This contains the effort level as an integer (Story Points). This is based on the Scrum "modified-Fibonacci" system of effort estimation.
     */
    int getEffort();

    /**
     * This contains a list of {@link Link}s for further information about the problem and its solution.
     */
    List<Link> getLinks();

    /**
     * This contains a list of {@link Quickfix}s for follow up in tools
     *
     * @return
     */
    Iterable<Quickfix> getQuickfixes();

    /**
     * This contains the line number of the problem.
     */
    int getLineNumber();

    /**
     * This contains the column number within that line.
     */
    int getColumn();

    /**
     * This contains the length of the code section being referenced. For example, if the original code was "f.a()", this would be be "5".
     */
    int getLength();

    /**
     * This contains the original source code itself (for example, "proprietaryobject.doStuff()").
     */
    String getSourceSnippit();

    /**
     * This contains the id of the rule that produced this {@link Classification}.
     */
    String getRuleID();
}
