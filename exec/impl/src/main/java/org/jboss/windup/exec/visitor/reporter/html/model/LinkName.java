package org.jboss.windup.exec.visitor.reporter.html.model;

public class LinkName extends Name
{
    private final String url;

    public LinkName(String url, String name)
    {
        super(name);
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }

    @Override
    public String toString()
    {
        return "<a href='" + url + "'>" + this.getName() + "</a>";
    }
}
