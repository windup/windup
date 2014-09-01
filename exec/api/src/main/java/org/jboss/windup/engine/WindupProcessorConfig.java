package org.jboss.windup.engine;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class WindupProcessorConfig {

    private Predicate<WindupRuleProvider> ruleProviderFilter;
    
    private WindupProgressMonitor progressMonitor = new NullWindupProgressMonitor();


    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    
    public Predicate<WindupRuleProvider> getRuleProviderFilter()
    {
        return ruleProviderFilter;
    }
    
    
    public WindupProcessorConfig setRuleProviderFilter(Predicate<WindupRuleProvider> ruleProviderFilter)
    {
        this.ruleProviderFilter = ruleProviderFilter;
        return this;
    }
    
    
    public WindupProgressMonitor getProgressMonitor()
    {
        return progressMonitor;
    }
    
    
    public WindupProcessorConfig setProgressMonitor(WindupProgressMonitor progressMonitor)
    {
        this.progressMonitor = progressMonitor;
        return this;
    }
    
    //</editor-fold>
    

}// class
