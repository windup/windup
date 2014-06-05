package org.jboss.windup.exec.visitor.reporter.html.model;

public class Tag
{

    private String title;
    private Level level;

    public Tag()
    {
    }

    public Tag(String title, Level level)
    {
        super();
        this.title = title;
        this.level = level;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Level getLevel()
    {
        return level;
    }

    public void setLevel(Level level)
    {
        this.level = level;
    }

}
