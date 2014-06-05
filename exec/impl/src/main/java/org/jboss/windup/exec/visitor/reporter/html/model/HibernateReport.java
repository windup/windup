package org.jboss.windup.exec.visitor.reporter.html.model;

import java.util.LinkedList;
import java.util.List;

public class HibernateReport {

    private String sessionName;
    private final List<SessionPropertyRow> sessionProperties = new LinkedList<>();
    private final List<HibernateEntityRow> hibernateEntities = new LinkedList<>();

    public String getSessionName() {
        return sessionName;
    }
    
    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }
    
    public List<SessionPropertyRow> getSessionProperties() {
        return sessionProperties;
    }
    
    public List<HibernateEntityRow> getHibernateEntities() {
        return hibernateEntities;
    }
    
    public static class SessionPropertyRow {
        private String property;
        private String value;
        
        public SessionPropertyRow() {

        }
        
        public SessionPropertyRow(String property, String value) {
            this.property = property;
            this.value = value;
        }
        
        public String getProperty() {
            return property;
        }
        public void setProperty(String property) {
            this.property = property;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }
    
    public static class HibernateEntityRow {

        private String qualifiedName;
        private String tableName;
        
        public HibernateEntityRow() {

        }
        
        public HibernateEntityRow(String qualifiedName, String tableName) {
            this.qualifiedName = qualifiedName;
            this.tableName = tableName;
        }
        
        public String getQualifiedName() {
            return qualifiedName;
        }
        public void setQualifiedName(String qualifiedName) {
            this.qualifiedName = qualifiedName;
        }
        public String getTableName() {
            return tableName;
        }
        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
        

        
    }
    
}
