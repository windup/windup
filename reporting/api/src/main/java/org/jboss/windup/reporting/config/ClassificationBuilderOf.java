package org.jboss.windup.reporting.config;

/**
 * Intermediate step for constructing {@link Classification} instances for a specified ref.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ClassificationBuilderOf
{
    private Classification classification;

    ClassificationBuilderOf(String variable)
    {
        this.classification = new Classification();
        this.classification.setInputVariableName(variable);
    }

    /**
     * Set the text of this {@link Classification}. E.g: "Unparsable XML file." or "Source File"
     */
    public Classification as(String classification)
    {
        this.classification.setClassificationText(classification);
        return this.classification;
    }
}
