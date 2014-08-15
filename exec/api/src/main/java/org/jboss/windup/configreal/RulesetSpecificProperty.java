package org.jboss.windup.configreal;


/**
 *  Triplet for module specific property, e.g --conf.logging.merge=true .
 */
public class RulesetSpecificProperty {
    private String rulesetId;
    private String propName;
    private String value;


    public RulesetSpecificProperty(String rulesetId, String propName, String value)
    {
        this.rulesetId = this.rulesetId;
        this.propName = propName;
        this.value = value;
    }


    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getRulesetId()
    {
        return rulesetId;
    }
    
    
    public void setRulesetId(String rulesetId)
    {
        this.rulesetId = rulesetId;
    }
    
    
    public String getPropName()
    {
        return propName;
    }
    
    
    public void setPropName(String propName)
    {
        this.propName = propName;
    }
    
    
    public String getValue()
    {
        return value;
    }
    
    
    public void setValue(String value)
    {
        this.value = value;
    }
    
    
    //</editor-fold>

}// class
