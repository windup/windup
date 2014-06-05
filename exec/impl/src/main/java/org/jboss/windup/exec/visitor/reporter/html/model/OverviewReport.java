package org.jboss.windup.exec.visitor.reporter.html.model;

import java.util.LinkedList;
import java.util.List;

public class OverviewReport
{

    private LinkName applicationLink;

    private final List<Tag> technologyTags = new LinkedList<>();
    private Effort effort = Effort.LOW;
    private final List<Tag> issueTags = new LinkedList<>();

    public LinkName getApplicationLink()
    {
        return applicationLink;
    }

    public void setApplicationLink(LinkName applicationLink)
    {
        this.applicationLink = applicationLink;
    }

    public List<Tag> getTechnologyTags()
    {
        return technologyTags;
    }

    public Effort getEffort()
    {
        return effort;
    }

    public void setEffort(Effort effort)
    {
        this.effort = effort;
    }

    public List<Tag> getIssueTags()
    {
        return issueTags;
    }

}
