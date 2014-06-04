package org.jboss.windup.engine.visitor.reporter.html.model;

import java.util.LinkedList;
import java.util.List;

public class ServerResourceReport
{

    private final List<JMSQueueRow> queues = new LinkedList<>();
    private final List<DatabaseRow> databases = new LinkedList<>();
    private final List<JMXRow> jmxBeans = new LinkedList<>();

    public List<JMSQueueRow> getQueues()
    {
        return queues;
    }

    public List<DatabaseRow> getDatabases()
    {
        return databases;
    }

    public List<JMXRow> getJmxBeans()
    {
        return jmxBeans;
    }

    public static class JMSQueueRow
    {
        public String queueName;
        public String jndiName;
        public String jmsType;

        public JMSQueueRow()
        {

        }

        public JMSQueueRow(String queueName, String jndiName, String jmsType)
        {
            this.queueName = queueName;
            this.jndiName = jndiName;
            this.jmsType = jmsType;
        }

        public String getQueueName()
        {
            return queueName;
        }

        public void setQueueName(String queueName)
        {
            this.queueName = queueName;
        }

        public String getJndiName()
        {
            return jndiName;
        }

        public void setJndiName(String jndiName)
        {
            this.jndiName = jndiName;
        }

        public String getJmsType()
        {
            return jmsType;
        }

        public void setJmsType(String jmsType)
        {
            this.jmsType = jmsType;
        }
    }

    public static class DatabaseRow
    {
        public String databaseType;
        public String jndiName;

        public DatabaseRow()
        {

        }

        public DatabaseRow(String databaseType, String jndiName)
        {
            this.databaseType = databaseType;
            this.jndiName = jndiName;
        }

        public String getDatabaseType()
        {
            return databaseType;
        }

        public void setDatabaseType(String databaseType)
        {
            this.databaseType = databaseType;
        }

        public String getJndiName()
        {
            return jndiName;
        }

        public void setJndiName(String jndiName)
        {
            this.jndiName = jndiName;
        }
    }

    public static class JMXRow
    {
        public JMXRow()
        {

        }

        public JMXRow(String jmxObjectName, String qualifiedName)
        {
            this.jmxObjectName = jmxObjectName;
            this.qualifiedName = qualifiedName;
        }

        public String jmxObjectName;
        public String qualifiedName;

        public String getJmxObjectName()
        {
            return jmxObjectName;
        }

        public String getQualifiedName()
        {
            return qualifiedName;
        }

        public void setQualifiedName(String qualifiedName)
        {
            this.qualifiedName = qualifiedName;
        }

        public void setJmxObjectName(String jmxObjectName)
        {
            this.jmxObjectName = jmxObjectName;
        }
    }
}
