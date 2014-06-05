package org.jboss.windup.exec.visitor.reporter.html.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ArchiveReport implements Comparable<ArchiveReport>
{

    private String applicationPath;
    private Level level = Level.PRIMARY;

    private final Set<ResourceReportRow> resources = new TreeSet<ResourceReportRow>();

    public String getApplicationPath()
    {
        return applicationPath;
    }

    public void setApplicationPath(String applicationPath)
    {
        this.applicationPath = applicationPath;
    }

    public Level getLevel()
    {
        return level;
    }

    public void setLevel(Level level)
    {
        this.level = level;
    }

    public Set<ResourceReportRow> getResources()
    {
        return resources;
    }

    public static class ResourceReportRow implements Comparable<ResourceReportRow>
    {

        private Name resourceName;

        private final List<Tag> technologyTags = new LinkedList<>();
        private Effort effort;
        private final List<Tag> issueTags = new LinkedList<>();

        public Name getResourceName()
        {
            return resourceName;
        }

        public void setResourceName(Name resourceName)
        {
            this.resourceName = resourceName;
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

        @Override
        public int compareTo(ResourceReportRow o)
        {
            return this.getResourceName().getName().compareTo(o.getResourceName().getName());
        }
    }

    @Override
    public int compareTo(ArchiveReport o)
    {
        return this.getApplicationPath().compareTo(o.getApplicationPath());
    }
}
