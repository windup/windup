package org.jboss.windup.reporting.config;

/**
 * Represents a link to an external resource, such as an URL.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Link
{
    private String link;
    private String description;

    private Link(String link, String description)
    {
        this.link = link;
        this.description = description;
    }

    /**
     * Create a new {@link Link} instance with the given target and description.
     */
    public static Link to(String description, String link)
    {
        return new Link(link, description);
    }

    /**
     * Get the {@link Link} value.
     */
    public String getLink()
    {
        return link;
    }

    /**
     * Get the description of this {@link Link}.
     */
    public String getDescription()
    {
        return description;
    }
}
