package org.jboss.windup.reporting.config;

/**
 * Used after {@link Hint#titled(String)} has been called.
 *
 */
public class HintBuilderTitle
{
    private String title;

    /**
     * Constructs an instance with the specified title.
     */
    public HintBuilderTitle(String title)
    {
        this.title = title;
    }

    /**
     * Sets the {@link Hint}s body content.
     */
    public HintText withText(String text)
    {
        Hint hint = new Hint();
        hint.setTitle(title);
        hint.setText(text);
        return hint;
    }
}
