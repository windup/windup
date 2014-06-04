package org.jboss.windup.engine.visitor.reporter.html.model;

import java.util.LinkedList;
import java.util.List;

public class SpringReport
{

    private final List<SpringBeanRow> springBeans = new LinkedList<>();
    private final List<SpringBeanJNDIRow> springJNDIBeans = new LinkedList<>();
    private final List<SpringBeanResourceRow> springResourceBeans = new LinkedList<>();

    public List<SpringBeanRow> getSpringBeans()
    {
        return springBeans;
    }

    public List<SpringBeanJNDIRow> getSpringJNDIBeans()
    {
        return springJNDIBeans;
    }

    public List<SpringBeanResourceRow> getSpringResourceBeans()
    {
        return springResourceBeans;
    }

    public static class SpringBeanJNDIRow
    {

        private String beanName;
        private String qualifiedName;
        private String jndiName;

        public SpringBeanJNDIRow()
        {

        }

        public SpringBeanJNDIRow(String beanName, String qualifiedName, String jndiName)
        {
            this.beanName = beanName;
            this.qualifiedName = qualifiedName;
            this.jndiName = jndiName;
        }

        public String getBeanName()
        {
            return beanName;
        }

        public void setBeanName(String beanName)
        {
            this.beanName = beanName;
        }

        public String getQualifiedName()
        {
            return qualifiedName;
        }

        public void setQualifiedName(String qualifiedName)
        {
            this.qualifiedName = qualifiedName;
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

    public static class SpringBeanRow
    {

        private String beanName;
        private String qualifiedName;

        public SpringBeanRow()
        {

        }

        public SpringBeanRow(String beanName, String qualifiedName)
        {
            this.beanName = beanName;
            this.qualifiedName = qualifiedName;
        }

        public String getBeanName()
        {
            return beanName;
        }

        public void setBeanName(String beanName)
        {
            this.beanName = beanName;
        }

        public String getQualifiedName()
        {
            return qualifiedName;
        }

        public void setQualifiedName(String qualifiedName)
        {
            this.qualifiedName = qualifiedName;
        }
    }

    public static class SpringBeanResourceRow
    {

        private String beanName;
        private String qualifiedName;
        private String resourceType;

        public SpringBeanResourceRow()
        {

        }

        public SpringBeanResourceRow(String beanName, String qualifiedName, String resourceType)
        {
            this.beanName = beanName;
            this.qualifiedName = qualifiedName;
            this.resourceType = resourceType;
        }

        public String getQualifiedName()
        {
            return qualifiedName;
        }

        public void setQualifiedName(String qualifiedName)
        {
            this.qualifiedName = qualifiedName;
        }

        public String getBeanName()
        {
            return beanName;
        }

        public void setBeanName(String beanName)
        {
            this.beanName = beanName;
        }

        public String getResourceType()
        {
            return resourceType;
        }

        public void setResourceType(String resourceType)
        {
            this.resourceType = resourceType;
        }
    }
}
