package org.jboss.windup.reporting.config.classification;

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
        this.classification = new Classification(variable);
    }

    /**
     * Set the text of this {@link Classification}. E.g: "Unparsable XML file." or "Source File"
     */
    public ClassificationAs as(String classification)
    {
        this.classification.setClassificationText(classification);
        return this.classification;
    }
}
