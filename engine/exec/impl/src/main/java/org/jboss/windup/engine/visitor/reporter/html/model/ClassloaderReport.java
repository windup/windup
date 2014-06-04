package org.jboss.windup.engine.visitor.reporter.html.model;

import java.util.LinkedList;
import java.util.List;

public class ClassloaderReport
{

    private final String type;
    private final String referencedFrom;
    private final String referenceType;

    private final List<ClassLoaderReportRow> classes;

    public ClassloaderReport(String type, String referencedFrom, String referenceType)
    {
        this.type = type;
        this.referencedFrom = referencedFrom;
        this.referenceType = referenceType;
        this.classes = new LinkedList<ClassLoaderReportRow>();
    }

    public String getType()
    {
        return type;
    }

    public List<ClassLoaderReportRow> getClasses()
    {
        return classes;
    }

    public String getReferencedFrom()
    {
        return referencedFrom;
    }

    public String getReferenceType()
    {
        return referenceType;
    }

    public static class ClassLoaderReportRow
    {
        private final Name clzName;
        private final List<ClassReference> references;

        public ClassLoaderReportRow(Name clzName)
        {
            this.clzName = clzName;
            this.references = new LinkedList<ClassReference>();
        }

        public Name getClzName()
        {
            return clzName;
        }

        public List<ClassReference> getReferences()
        {
            return references;
        }
    }

    public static class ClassReference
    {
        private final String referenceType;
        private final Name clzName;

        public ClassReference(String referenceType, Name clzName)
        {
            this.referenceType = referenceType;
            this.clzName = clzName;
        }

        public String getReferenceType()
        {
            return referenceType;
        }

        public Name getClzName()
        {
            return clzName;
        }

    }
}
