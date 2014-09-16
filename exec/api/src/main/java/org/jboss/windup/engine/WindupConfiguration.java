package org.jboss.windup.engine;

import java.nio.file.Path;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphLifecycleListener;

/**
 * Configuration of WindupProcessor.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class WindupConfiguration
{
    private GraphLifecycleListener graphListener;
    private Predicate<WindupRuleProvider> ruleProviderFilter;
    private WindupProgressMonitor progressMonitor = new NullWindupProgressMonitor();
    private Path outputDirectory;
    private GraphContext context;

    public GraphLifecycleListener getGraphListener()
    {
        return graphListener;
    }

    /**
     * Set GraphLifecycleListener to the given one. Can be used to fill the graph with data after it was initialized.
     */
    public WindupConfiguration setGraphListener(GraphLifecycleListener graphListener)
    {
        this.graphListener = graphListener;
        return this;
    }

    public Path getOutputDirectory()
    {
        return outputDirectory;
    }

    /**
     * Sets the directory to put the output to (migration report, temporary files, exported graph data...).
     */
    public WindupConfiguration setOutputDirectory(Path outputDirectory)
    {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public Predicate<WindupRuleProvider> getRuleProviderFilter()
    {
        return ruleProviderFilter;
    }

    /**
     * A filter to limit which rule providers' rules will be executed.
     */
    public WindupConfiguration setRuleProviderFilter(Predicate<WindupRuleProvider> ruleProviderFilter)
    {
        this.ruleProviderFilter = ruleProviderFilter;
        return this;
    }

    public WindupProgressMonitor getProgressMonitor()
    {
        return progressMonitor;
    }

    /**
     * A progress monitor which will get notification of the rule execution progress.
     */
    public WindupConfiguration setProgressMonitor(WindupProgressMonitor progressMonitor)
    {
        this.progressMonitor = progressMonitor;
        return this;
    }

    /**
     * Sets the {@link GraphContext} instance on which the {@link WindupProcessor} should execute.
     */
    public WindupConfiguration setGraphContext(GraphContext context)
    {
        this.context = context;
        return this;
    }

    /**
     * Gets the {@link GraphContext} instance on which the {@link WindupProcessor} should execute.
     */
    public GraphContext getGraphContext()
    {
        return context;
    }

}
