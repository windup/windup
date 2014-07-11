package org.jboss.windup.rules.apps.java.blacklist;

public class Link
{
    private String link;
    private String description;
    
    public Link (String link, String description) {
        this.link=link;
        this.description=description;
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
     * Static constructor with a cooler name
     */
    public static Link to(String link, String description) {
        return new Link(link,description);
    }
}
