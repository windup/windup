package org.jboss.windup.reporting.config;

/**
 * Fluent builder for {@link Classification} config element.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface ClassificationBuilder
{
    /**
     * Set the label, or text of this {@link Classification}.
     */
    Classification as(String title);
}
