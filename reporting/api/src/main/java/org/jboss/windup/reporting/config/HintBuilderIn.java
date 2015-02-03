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
     * Sets the title of the {@link Hint}.
     */
    public HintBuilderTitle titled(String title)
    {
        return new HintBuilderTitle(title);
    }

    /**
     * Specify the text or content to be displayed in the report on the line for which the hint is added.
     */
    public HintText withText(String text)
    {
        hint.setText(text);
        return hint;
    }
}
