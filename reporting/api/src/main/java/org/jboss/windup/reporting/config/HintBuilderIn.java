package org.jboss.windup.reporting.config;

/**
 * Intermediate step for building {@link Hint} instances.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class HintBuilderIn
{
    private Hint hint;

    public HintBuilderIn(String fileVariable)
    {
        this.hint = new Hint(fileVariable);
    }

    /**
     * Specify the text or content to be displayed in the report.
     */
    public Hint withText(String text)
    {
        hint.setText(text);
        return hint;
    }
}
