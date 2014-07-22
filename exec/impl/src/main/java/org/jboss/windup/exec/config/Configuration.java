package org.jboss.windup.exec.config;

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *  Holds global configuration and plugin-specific configuration.
 * 
 *  @author Ondrej Zizka
 */
@XmlRootElement(name="config")
@XmlAccessorType( XmlAccessType.NONE )
public class Configuration {

    @XmlElement
    private CoreConfiguration globalConfig = new CoreConfiguration();

    private List<RulesetSpecificProperty> rulesetConfigs = new LinkedList();
    
    
    
    /**
     *  What to do if some resource already exists.
     *  MERGE (ModelNode into current model) and ASK (interactive) are not supported yet.
     */
    public enum IfExists {
        FAIL, WARN, SKIP, MERGE, OVERWRITE, ASK, GUI;
        
        /** The same as valueOf(), only case-insensitive. */
        public static IfExists valueOf_Custom(String str) throws IllegalArgumentException {
            try {
                return valueOf( str.toUpperCase() );
            }
            catch( IllegalArgumentException | NullPointerException ex ){
                throw new IllegalArgumentException("ifExists must be one of FAIL, WARN, SKIP, MERGE, OVERWRITE, ASK. Was: " + str);
            }
        }
        
        public static final String PARAM_NAME = "ifExists";
    }// enum


    
    
    /**
     *  Triplet for module specific property, e.g --conf.logging.merge=true .
     */
    public static class RulesetSpecificProperty{
        
        private String moduleId;
        private String propName;
        private String value;

        public RulesetSpecificProperty(String moduleId, String propName, String value) {
            this.moduleId = moduleId;
            this.propName = propName;
            this.value = value;
        }

        public String getModuleId() { return moduleId; }
        public void setModuleId(String moduleId) { this.moduleId = moduleId; }
        public String getPropName() { return propName; }
        public void setPropName(String propName) { this.propName = propName; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }        
    }
    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public CoreConfiguration getGlobal() { return globalConfig; }
    public void setGlobalConfig(CoreConfiguration options) { this.globalConfig = options; }
    public List<RulesetSpecificProperty> getModuleConfigs() { return rulesetConfigs; }
    public void setModuleConfigs(List<RulesetSpecificProperty> moduleConfigs) { this.rulesetConfigs = moduleConfigs; }
    //</editor-fold>    
    
}// class
