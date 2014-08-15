package org.jboss.windup.rules.apps.java.reporting;

/**
 * Intermediate step for building {@link JavaHint} instances.
 * 
 * @author Jess Sightler <jesse.sightler@gmail.com>
 */
public class JavaHintBuilderIn
{
    private JavaHint hint;

    public JavaHintBuilderIn(String fileVariable)
    {
        this.hint = new JavaHint(fileVariable);
    }

    /**
     * Specify the text or content to be displayed in the report.
     */
    public JavaHint withText(String text)
    {
        hint.setText(text);
        return hint;
    }
}
