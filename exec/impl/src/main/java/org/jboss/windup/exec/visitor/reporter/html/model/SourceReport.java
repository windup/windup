package org.jboss.windup.exec.visitor.reporter.html.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

public class SourceReport
{

    private String sourceType;
    private String sourceBlock;
    private String sourceName;
    private String sourceBody;

    private final Set<SourceLineAnnotations> sourceLineAnnotations = new TreeSet<SourceLineAnnotations>();

    public String getSourceBlock()
    {
        return sourceBlock;
    }

    public void setSourceBlock(String sourceBlock)
    {
        this.sourceBlock = sourceBlock;
    }

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }

    public String getSourceBody()
    {
        return sourceBody;
    }

    public void setSourceBody(String sourceBody)
    {
        this.sourceBody = sourceBody;
    }

    public Set<SourceLineAnnotations> getSourceLineAnnotations()
    {
        return sourceLineAnnotations;
    }

    public static class SourceLineAnnotations implements Comparable<SourceLineAnnotations>
    {

        private final int lineNumber;
        private final String title;
        private final String level;
        private final Collection<SourceLineAnnotationHint> hints = new LinkedList<SourceLineAnnotationHint>();

        public SourceLineAnnotations(int lineNumber, String title, String level)
        {
            this.lineNumber = lineNumber;
            this.title = title;
            this.level = level;
        }

        public int getLineNumber()
        {
            return lineNumber;
        }

        public String getTitle()
        {
            return title;
        }

        public Collection<SourceLineAnnotationHint> getHints()
        {
            return hints;
        }

        public String getLevel()
        {
            return level;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((hints == null) ? 0 : hints.hashCode());
            result = prime * result + lineNumber;
            result = prime * result + ((title == null) ? 0 : title.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SourceLineAnnotations other = (SourceLineAnnotations) obj;
            if (hints == null)
            {
                if (other.hints != null)
                    return false;
            }
            else if (!hints.equals(other.hints))
                return false;
            if (lineNumber != other.lineNumber)
                return false;
            if (title == null)
            {
                if (other.title != null)
                    return false;
            }
            else if (!title.equals(other.title))
                return false;
            return true;
        }

        @Override
        public int compareTo(SourceLineAnnotations o)
        {
            return Integer.compare(this.lineNumber, o.lineNumber);
        }

    }

    public static class SourceLineAnnotationHint
    {

        private final String description;

        public SourceLineAnnotationHint(String description)
        {
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }

        @Override
        public String toString()
        {
            return description;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result
                        + ((description == null) ? 0 : description.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SourceLineAnnotationHint other = (SourceLineAnnotationHint) obj;
            if (description == null)
            {
                if (other.description != null)
                    return false;
            }
            else if (!description.equals(other.description))
                return false;
            return true;
        }

    }

}
