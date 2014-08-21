package org.jboss.windup.configreal;


import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class WindupConfig {
    private static final Logger log = LoggerFactory.getLogger( WindupConfig.class );
    
    
    private CoreConfig coreConfig = new CoreConfig();
    
    private List<RulesetSpecificProperty> rulesetConfigs;


    
    
    public CoreConfig getCoreConfig()
    {
        return coreConfig;
    }


    public void setCoreConfig(CoreConfig coreConfig)
    {
        this.coreConfig = coreConfig;
    }


    public List<RulesetSpecificProperty> getRulesetConfigs()
    {
        return rulesetConfigs;
    }


    public void setRulesetConfigs(List<RulesetSpecificProperty> rulesetConfigs)
    {
        this.rulesetConfigs = rulesetConfigs;
    }

}// class
