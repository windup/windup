package org.jboss.windup.tooling;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.util.Lists;
import org.jboss.windup.config.SkipReportsRenderingOption;
import org.jboss.windup.config.loader.RuleLoader;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleProviderRegistryCache;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.config.ExcludePackagesOption;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.tooling.quickfix.QuickfixLocationDTO;
import org.jboss.windup.tooling.quickfix.QuickfixService;
import org.jboss.windup.tooling.rules.RuleProviderRegistry;
import org.jboss.windup.tooling.rules.RuleProviderRegistryImpl;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.Util;
import org.jboss.windup.util.exception.WindupException;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ExecutionBuilderImpl implements ExecutionBuilder
{
    @Inject
    private GraphContextFactory graphContextFactory;

    @Inject
    private ToolingXMLService toolingXMLService;

    @Inject
    private WindupProcessor processor;

    @Inject
    private Furnace furnace;

    @Inject
    private RuleProviderRegistryCache ruleProviderCache;

    private String windupHome;
    private WindupToolingProgressMonitor progressMonitor;
    private String input;
    private String output;
    private Set<String> ignorePathPatterns = new HashSet<>();
    private Set<String> includePackagePrefixSet = new HashSet<>();
    private Set<String> excludePackagePrefixSet = new HashSet<>();
    private Set<String> userRulesPathSet = new HashSet<>();
    private Map<String, Object> options = new HashMap<>();

    private String version;

    @Inject
    private RuleLoader ruleLoader;

    @Inject
    private QuickfixService quickfixService;

    @Override
    public void clear() throws RemoteException
    {
        this.windupHome = null;
        this.progressMonitor = null;
        this.input = null;
        this.output = null;
        this.ignorePathPatterns.clear();
        this.includePackagePrefixSet.clear();
        this.excludePackagePrefixSet.clear();
        this.userRulesPathSet.clear();
        this.options.clear();
    }

    // TODO: Should we also do UnicastRemoteObject.unexportObject(this, true)?
    @Override
    public void terminate() throws RemoteException
    {
        furnace.stop();
        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
        }
        Runtime.getRuntime().halt(1);
    }

    @Override
    public void setWindupHome(String windupHome) throws RemoteException
    {
        this.windupHome = windupHome;
    }

    @Override
    public void setInput(String input) throws RemoteException
    {
        this.input = input;
    }

    @Override
    public void setOutput(String output) throws RemoteException
    {
        this.output = output;
    }

    @Override
    public void ignore(String ignorePattern) throws RemoteException
    {
        this.ignorePathPatterns.add(ignorePattern);
    }

    @Override
    public void includePackage(String packagePrefix) throws RemoteException
    {
        this.includePackagePrefixSet.add(packagePrefix);
    }

    @Override
    public void includePackages(Collection<String> includePackagePrefixes) throws RemoteException
    {
        if (includePackagePrefixes != null)
            this.includePackagePrefixSet.addAll(includePackagePrefixes);
    }

    @Override
    public void excludePackage(String packagePrefix) throws RemoteException
    {
        this.excludePackagePrefixSet.add(packagePrefix);
    }

    @Override
    public void excludePackages(Collection<String> excludePackagePrefixes) throws RemoteException
    {
        if (excludePackagePrefixes != null)
            this.excludePackagePrefixSet.addAll(excludePackagePrefixes);
    }

    @Override
    public void setProgressMonitor(WindupToolingProgressMonitor monitor) throws RemoteException
    {
        this.progressMonitor = monitor;
    }

    @Override
    public void sourceOnlyMode() throws RemoteException
    {
        options.put(SourceModeOption.NAME, true);
    }

    @Override
    public void skipReportGeneration() throws RemoteException
    {
        options.put(SkipReportsRenderingOption.NAME, true);
    }

    @Override
    public void addUserRulesPath(String rulesPath) throws RemoteException
    {
        if (rulesPath == null)
            return;

        String pathString = Paths.get(rulesPath).normalize().toAbsolutePath().toString();
        this.userRulesPathSet.add(pathString);
    }

    @Override
    public void addUserRulesPaths(Iterable<String> rulesPaths) throws RemoteException
    {
        if (rulesPaths == null)
            return;

        for (String rulesPath : rulesPaths)
        {
            this.addUserRulesPath(rulesPath);
        }
    }

    @Override
    public void setOption(String name, Object value) throws RemoteException
    {
        this.options.put(name, value);
    }

    @Override
    public String getVersion() throws RemoteException
    {
        return version;
    }

    @Override
    public void setVersion(String version) throws RemoteException
    {
        this.version = version;
    }

    public String transform(String transformationID, QuickfixLocationDTO locationDTO) throws RemoteException
    {
        return quickfixService.transform(transformationID, locationDTO);
    }

    @Override
    public ExecutionResults execute() throws RemoteException
    {
        PathUtil.setWindupHome(Paths.get(this.windupHome));
        WindupConfiguration windupConfiguration = new WindupConfiguration();
        try
        {
            windupConfiguration.useDefaultDirectories();
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to configure " + Util.WINDUP_BRAND_NAME_ACRONYM + " due to: " + e.getMessage(), e);
        }
        ToolingProgressMonitorAdapter progressMonitorAdapter = new ToolingProgressMonitorAdapter(this.progressMonitor);

        windupConfiguration.addInputPath(Paths.get(this.input));
        windupConfiguration.setOutputDirectory(Paths.get(this.output));
        windupConfiguration.setProgressMonitor(progressMonitorAdapter);

        Path graphPath = Paths.get(output).resolve(GraphContextFactory.DEFAULT_GRAPH_SUBDIRECTORY);

        Logger globalLogger = Logger.getLogger("");
        WindupProgressLoggingHandler loggingHandler = null;
        if (progressMonitor instanceof WindupToolingProgressMonitor)
        {
            loggingHandler = new WindupProgressLoggingHandler((WindupToolingProgressMonitor) progressMonitor);
            globalLogger.addHandler(loggingHandler);
        }

        try (final GraphContext graphContext = graphContextFactory.create(graphPath))
        {

            GraphService<IgnoredFileRegexModel> graphService = new GraphService<>(graphContext, IgnoredFileRegexModel.class);
            for (String ignorePattern : this.ignorePathPatterns)
            {
                IgnoredFileRegexModel ignored = graphService.create();
                ignored.setRegex(ignorePattern);

                WindupJavaConfigurationModel javaCfg = WindupJavaConfigurationService.getJavaConfigurationModel(graphContext);
                javaCfg.addIgnoredFileRegex(ignored);
            }

            windupConfiguration.setOptionValue(ScanPackagesOption.NAME, Lists.toList(this.includePackagePrefixSet));
            windupConfiguration.setOptionValue(ExcludePackagesOption.NAME, Lists.toList(this.excludePackagePrefixSet));

            for (Map.Entry<String, Object> option : options.entrySet())
            {
                windupConfiguration.setOptionValue(option.getKey(), option.getValue());
            }
            windupConfiguration.setGraphContext(graphContext);
            processor.execute(windupConfiguration);

            return new ExecutionResultsImpl(graphContext, toolingXMLService);
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to instantiate graph due to: " + e.getMessage(), e);
        }
        finally
        {
            if (loggingHandler != null)
                globalLogger.removeHandler(loggingHandler);
        }
    }

    @Override
    public RuleProviderRegistry getRuleProviderRegistry(List<String> pathStrings) throws RemoteException
    {
        RuleProviderRegistryImpl ruleProviderRegistry = new RuleProviderRegistryImpl();
        List<Path> paths = pathStrings.stream().map(pathString -> Paths.get(pathString)).collect(Collectors.toList());

        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(paths, null);
        org.jboss.windup.config.metadata.RuleProviderRegistry registry = this.ruleLoader.loadConfiguration(ruleLoaderContext);

        ruleProviderRegistry.buildRuleProviders(registry);
        return ruleProviderRegistry;
    }

    @Override
    public RuleProviderRegistry getSystemRuleProviderRegistry() throws RemoteException
    {
        RuleProviderRegistryImpl ruleProviderRegistry = new RuleProviderRegistryImpl();
        ruleProviderRegistry.buildRuleProviders(ruleProviderCache.getRuleProviderRegistry());
        return ruleProviderRegistry;
    }

    private class WindupProgressLoggingHandler extends Handler
    {
        private final WindupToolingProgressMonitor monitor;

        public WindupProgressLoggingHandler(WindupToolingProgressMonitor monitor)
        {
            this.monitor = monitor;
        }

        @Override
        public void publish(LogRecord record)
        {
            if (this.monitor == null)
                return;

            try
            {
                this.monitor.logMessage(record);
            }
            catch (RemoteException e)
            {
            }
        }

        @Override
        public void flush()
        {

        }

        @Override
        public void close() throws SecurityException
        {

        }
    }
}
