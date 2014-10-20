package org.jboss.windup.exec.configuration;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupConfigurationOption;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.furnace.FurnaceHolder;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.exec.configuration.options.OfflineModeOption;
import org.jboss.windup.exec.configuration.options.OutputPathOption;
import org.jboss.windup.exec.configuration.options.UserRulesDirectoryOption;
import org.jboss.windup.graph.GraphContext;

/**
 * Configuration of WindupProcessor.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class WindupConfiguration
{
    private Predicate<WindupRuleProvider> ruleProviderFilter;
    private WindupProgressMonitor progressMonitor = new NullWindupProgressMonitor();
    private Map<String, Object> configurationOptions = new HashMap<>();

    private GraphContext context;

    /**
     * Sets a configuration option to the specified value.
     */
    public WindupConfiguration setOptionValue(String name, Object value)
    {
        configurationOptions.put(name, value);
        return this;
    }

    /**
     * Returns the configuration value with the specified name.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOptionValue(String name)
    {
        return (T) configurationOptions.get(name);
    }

    /**
     * Returns all configuration options as an immutable {@link Map}.
     */
    public Map<String, Object> getOptionMap()
    {
        return Collections.unmodifiableMap(configurationOptions);
    }

    /**
     * Returns all of the {@link WindupConfigurationOption} in all currently available {@link Addon}s.
     */
    public static Iterable<WindupConfigurationOption> getWindupConfigurationOptions()
    {
        List<WindupConfigurationOption> results = new ArrayList<>();
        for (WindupConfigurationOption option : FurnaceHolder.getFurnace().getAddonRegistry()
                    .getServices(WindupConfigurationOption.class))
        {
            results.add(option);
        }
        Collections.sort(results, new Comparator<WindupConfigurationOption>()
        {
            @Override
            public int compare(WindupConfigurationOption o1, WindupConfigurationOption o2)
            {
                return o2.getPriority() - o1.getPriority();
            }
        });
        return results;
    }

    /**
     * Returns all of the {@link WindupConfigurationOption} in the specified {@link Addon}.
     */
    public static Iterable<WindupConfigurationOption> getWindupConfigurationOptions(Addon addon)
    {
        IdentityHashMap<ClassLoader, Addon> classLoaderToAddon = new IdentityHashMap<>();
        for (Addon loadedAddon : FurnaceHolder.getAddonRegistry().getAddons())
        {
            classLoaderToAddon.put(loadedAddon.getClassLoader(), loadedAddon);
        }

        List<WindupConfigurationOption> results = new ArrayList<>();
        Imported<WindupConfigurationOption> options = FurnaceHolder.getAddonRegistry()
                    .getServices(WindupConfigurationOption.class);
        for (WindupConfigurationOption option : options)
        {
            ClassLoader optionClassLoader = option.getClass().getClassLoader();
            Addon optionAddon = classLoaderToAddon.get(optionClassLoader);
            if (optionAddon.equals(addon))
            {
                results.add(option);
            }
        }
        return results;
    }

    /**
     * Contains the path to the input file (or directory) to be processed
     */
    public WindupConfiguration setInputPath(Path inputPath)
    {
        setOptionValue(InputPathOption.NAME, inputPath.toFile());
        return this;
    }

    /**
     * Contains the path to the input file (or directory) to be processed
     */
    public Path getInputPath()
    {
        File file = getOptionValue(InputPathOption.NAME);
        return file == null ? null : file.toPath();
    }

    /**
     * Contains the directory to put the output to (migration report, temporary files, exported graph data...).
     */
    public Path getOutputDirectory()
    {
        File file = getOptionValue(OutputPathOption.NAME);
        return file == null ? null : file.toPath();
    }

    /**
     * Contains the directory to put the output to (migration report, temporary files, exported graph data...).
     */
    public WindupConfiguration setOutputDirectory(Path outputDirectory)
    {
        setOptionValue(OutputPathOption.NAME, outputDirectory.toFile());
        return this;
    }

    /**
     * Contains a list of {@link Path}s with the directory that contains user provided rules.
     */
    public List<Path> getUserRulesDirectories()
    {
        List<Path> paths = getOptionValue(UserRulesDirectoryOption.NAME);
        if (paths == null)
        {
            return Collections.emptyList();
        }
        return paths;
    }

    /**
     * Contains a list of {@link Path}s with the directory that contains user provided rules.
     */
    public WindupConfiguration setUserRulesDirectories(List<Path> userRulesDirectories)
    {
        setOptionValue(UserRulesDirectoryOption.NAME, userRulesDirectories);
        return this;
    }

    /**
     * Contains a list of {@link Path}s with the directory that contains user provided rules.
     * 
     * This method does guard against duplicate directories.
     */
    public WindupConfiguration addUserRulesDirectory(Path path)
    {
        List<Path> paths = getOptionValue(UserRulesDirectoryOption.NAME);
        if (paths == null)
        {
            paths = new ArrayList<>();
            paths.add(path);
            return this;
        }

        for (Path existingPath : paths)
        {
            if (existingPath.equals(path))
            {
                return this;
            }
        }
        paths.add(path);

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

    public WindupConfiguration setOffline(boolean offline)
    {
        setOptionValue(OfflineModeOption.NAME, offline);
        return this;
    }

    public boolean isOffline()
    {
        Boolean offline = getOptionValue(OfflineModeOption.NAME);
        return offline == null ? false : offline;
    }
}
