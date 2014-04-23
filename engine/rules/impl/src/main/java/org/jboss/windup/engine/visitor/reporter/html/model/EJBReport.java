package org.jboss.windup.engine.visitor.reporter.html.model;

import java.util.LinkedList;
import java.util.List;

public class EJBReport
{

    private final List<EJBRow> statefulBeans = new LinkedList<>();
    private final List<EJBRow> statelessBeans = new LinkedList<>();
    private final List<MDBRow> mdbs = new LinkedList<>();

    public List<MDBRow> getMdbs()
    {
        return mdbs;
    }

    public List<EJBRow> getStatefulBeans()
    {
        return statefulBeans;
    }

    public List<EJBRow> getStatelessBeans()
    {
        return statelessBeans;
    }

    public static class EJBRow
    {
        private final String name;
        private final Name qualifiedName;
        private final String ejbType;

        public EJBRow(String name, Name qualifiedName, String ejbType)
        {
            this.name = name;
            this.qualifiedName = qualifiedName;
            this.ejbType = ejbType;
        }

        public String getName()
        {
            return name;
        }

        public Name getQualifiedName()
        {
            return qualifiedName;
        }

        public String getEjbType()
        {
            return ejbType;
        }
    }

    public static class MDBRow
    {

        private String name;
        private Name qualifiedName;
        private String queueName;

        public MDBRow()
        {

        }

        public MDBRow(String name, Name qualifiedName, String queueName)
        {
            this.name = name;
            this.qualifiedName = qualifiedName;
            this.queueName = queueName;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public Name getQualifiedName()
        {
            return qualifiedName;
        }

        public void setQualifiedName(Name qualifiedName)
        {
            this.qualifiedName = qualifiedName;
        }

        public String getQueueName()
        {
            return queueName;
        }

        public void setQueueName(String queueName)
        {
            this.queueName = queueName;
        }
    }

}
