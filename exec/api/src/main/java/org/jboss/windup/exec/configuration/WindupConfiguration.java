package org.jboss.windup.exec.configuration;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.ConfigurationOption;
import org.jboss.windup.config.furnace.FurnaceHolder;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.exec.configuration.options.OfflineModeOption;
import org.jboss.windup.exec.configuration.options.OutputPathOption;
import org.jboss.windup.exec.configuration.options.UserIgnorePathOption;
import org.jboss.windup.exec.configuration.options.UserRulesDirectoryOption;
import org.jboss.windup.exec.rulefilters.RuleProviderFilter;
import org.jboss.windup.graph.GraphContext;

/**
 * Configuration of WindupProcessor.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class WindupConfiguration
{
    private static final String DEFAULT_USER_RULES_DIRECTORIES_OPTION = "defaultUserRulesDirectories";
    private static final String DEFAULT_USER_IGNORE_DIRECTORIES_OPTION = "defaultUserIgnorePaths";

    private RuleProviderFilter ruleProviderFilter;
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
     * Returns all of the {@link ConfigurationOption} in all currently available {@link Addon}s.
     */
    public static Iterable<ConfigurationOption> getWindupConfigurationOptions()
    {
        return getWindupConfigurationOptions(FurnaceHolder.getFurnace());
    }

    /**
     * Returns all of the {@link ConfigurationOption} in all currently available {@link Addon}s.
     */
    public static Iterable<ConfigurationOption> getWindupConfigurationOptions(Furnace furnace)
    {
        List<ConfigurationOption> results = new ArrayList<>();
        for (ConfigurationOption option : furnace.getAddonRegistry().getServices(ConfigurationOption.class))
        {
            results.add(option);
        }
        Collections.sort(results, new Comparator<ConfigurationOption>()
        {
            @Override
            public int compare(ConfigurationOption o1, ConfigurationOption o2)
            {
                return o2.getPriority() - o1.getPriority();
            }
        });
        return results;
    }

    /**
     * Returns all of the {@link ConfigurationOption} in the specified {@link Addon}.
     */
    public static Iterable<ConfigurationOption> getWindupConfigurationOptions(Addon addon)
    {
        IdentityHashMap<ClassLoader, Addon> classLoaderToAddon = new IdentityHashMap<>();
        for (Addon loadedAddon : FurnaceHolder.getAddonRegistry().getAddons())
        {
            classLoaderToAddon.put(loadedAddon.getClassLoader(), loadedAddon);
        }

        List<ConfigurationOption> results = new ArrayList<>();
        Imported<ConfigurationOption> options = FurnaceHolder.getAddonRegistry()
                    .getServices(ConfigurationOption.class);
        for (ConfigurationOption option : options)
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
     * Gets all user rule directories. This includes both the ones that they specify (eg, /path/to/rules) as well as
     * ones that Windup provides by default (eg, WINDUP_HOME/rules and ~/.windup/rules).
     */
    public Iterable<Path> getAllUserRulesDirectories()
    {
        Set<Path> results = new HashSet<>();
        results.addAll(getDefaultUserRulesDirectories());
        File userSpecifiedFile = getOptionValue(UserRulesDirectoryOption.NAME);
        if (userSpecifiedFile != null)
        {
            results.add(userSpecifiedFile.toPath());
        }
        return results;
    }

    /**
     * Gets all the directories/files in which the regexes for ignoring the files is placed. This includes the
     * file/directory specified by the user and the default paths that are WINDUP_HOME/ignore and ~/.windup/ignore.
     *
     * @return
     */
    public Iterable<Path> getAllIgnoreDirectories()
    {
        Set<Path> results = new HashSet<>();
        results.addAll(getDefaultUserIgnoreDirectories());
        File userSpecifiedFile = getOptionValue(UserIgnorePathOption.NAME);
        if (userSpecifiedFile != null)
        {
            results.add(userSpecifiedFile.toPath());
        }
        return results;
    }

    /**
     * Contains a list of {@link Path}s with directories that contains user provided rules.
     */
    public List<Path> getDefaultUserRulesDirectories()
    {
        List<Path> paths = getOptionValue(DEFAULT_USER_RULES_DIRECTORIES_OPTION);
        if (paths == null)
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(paths);
    }

    /**
     * Contains a default list of {@link Path}s with directories/files that contains files having regexes of file names
     * to be ignored.
     */
    public List<Path> getDefaultUserIgnoreDirectories()
    {
        List<Path> paths = getOptionValue(DEFAULT_USER_IGNORE_DIRECTORIES_OPTION);
        if (paths == null)
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(paths);
    }

    /**
     * Contains a list of {@link Path}s with the directory that contains user provided rules.
     *
     * This method does guard against duplicate directories.
     */
    public WindupConfiguration addDefaultUserRulesDirectory(Path path)
    {
        List<Path> paths = getOptionValue(DEFAULT_USER_RULES_DIRECTORIES_OPTION);
        if (paths == null)
        {
            paths = new ArrayList<>();
            setOptionValue(DEFAULT_USER_RULES_DIRECTORIES_OPTION, paths);
        }

        File userSpecifiedRulePath = getOptionValue(UserRulesDirectoryOption.NAME);
        if (userSpecifiedRulePath != null && userSpecifiedRulePath.toPath().equals(path))
        {
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

    /**
     * Adds a path to the list of default {@link Path}s with directories/files that contain files with regexes of file
     * names to be ignored.
     *
     * This method does guard against duplicate directories.
     */
    public WindupConfiguration addDefaultUserIgnorePath(Path path)
    {
        List<Path> paths = getOptionValue(DEFAULT_USER_IGNORE_DIRECTORIES_OPTION);
        if (paths == null)
        {
            paths = new ArrayList<>();
            setOptionValue(DEFAULT_USER_IGNORE_DIRECTORIES_OPTION, paths);
        }

        File userSpecifiedIgnorePath = getOptionValue(UserIgnorePathOption.NAME);
        if (userSpecifiedIgnorePath != null && userSpecifiedIgnorePath.toPath().equals(path))
        {
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

    public RuleProviderFilter getRuleProviderFilter()
    {
        return ruleProviderFilter;
    }

    /**
     * A filter to limit which rule providers' rules will be executed.
     */
    public WindupConfiguration setRuleProviderFilter(RuleProviderFilter ruleProviderFilter)
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


    public void getOptionMap(String NAME)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
