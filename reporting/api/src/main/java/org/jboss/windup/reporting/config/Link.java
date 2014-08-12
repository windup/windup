package org.jboss.windup.reporting.config;

/**
 * Represents a link to an external resource.
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

    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Create a new {@link Link} instance with the given target and description.
     */
    public static Link to(String description, String link)
    {
        return new Link(link, description);
    }
}
