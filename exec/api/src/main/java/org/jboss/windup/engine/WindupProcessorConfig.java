package org.jboss.windup.engine;

import java.nio.file.Path;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class WindupProcessorConfig {

    private Predicate<WindupRuleProvider> ruleProviderFilter;
    
    private WindupProgressMonitor progressMonitor = new NullWindupProgressMonitor();
    
    private Path outputDirectory;


    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    
    public Path getOutputDirectory()
    {
        return outputDirectory;
    }

    public WindupProcessorConfig setOutputDirectory(Path outputDirectory)
    {
        this.outputDirectory = outputDirectory;
        return this;
    }


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
