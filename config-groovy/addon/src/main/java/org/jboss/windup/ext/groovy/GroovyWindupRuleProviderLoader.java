package org.jboss.windup.ext.groovy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonDependency;
import org.jboss.forge.furnace.addons.AddonFilter;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.builder.RuleProviderBuilder;
import org.jboss.windup.config.loader.RuleProviderLoader;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.FurnaceCompositeClassLoader;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.furnace.FurnaceClasspathScanner;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * Loads files with the specified extension (specified in {@link GroovyWindupRuleProviderLoader#GROOVY_RULES_EXTENSION}
 * ), interprets them as Groovy scripts, and returns the resulting {@link AbstractRuleProvider}s.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class GroovyWindupRuleProviderLoader implements RuleProviderLoader
{
    private static final Logger LOG = Logging.get(GroovyWindupRuleProviderLoader.class);

    public static final String CURRENT_WINDUP_SCRIPT = "CURRENT_WINDUP_SCRIPT";

    private static final String GROOVY_RULES_EXTENSION = "windup.groovy";

    @Inject
    private FurnaceClasspathScanner scanner;
    @Inject
    private Furnace furnace;

    @Inject
    private Imported<GroovyConfigMethod> methods;

    @Override
    @SuppressWarnings("unchecked")
    public List<RuleProvider> getProviders(final GraphContext context)
    {
        final List<RuleProvider> results = new ArrayList<>();

        Binding binding = new Binding();
        binding.setVariable("supportFunctions", new HashMap<>());
        binding.setVariable("graphContext", context);

        GroovyConfigContext configContext = new GroovyConfigContext()
        {
            @Override
            public GraphContext getGraphContext()
            {
                return context;
            }
        };

        for (GroovyConfigMethod method : methods)
        {
            binding.setVariable(method.getName(configContext), method.getClosure(configContext));
        }

        CompilerConfiguration config = new CompilerConfiguration();
        // TODO import everything!!! config.addCompilationCustomizers(new ImportCustomizer());
        ClassLoader loader = getCompositeClassloader();
        GroovyShell shell = new GroovyShell(loader, binding, config);

        // import all the support functions defined in a separate groovy file
        try (InputStream supportFuncsIS = getClass().getResourceAsStream(
                    "/org/jboss/windup/addon/groovy/WindupGroovySupportFunctions.groovy"))
        {
            InputStreamReader isr = new InputStreamReader(supportFuncsIS);
            shell.evaluate(isr);

        }
        catch (Exception e)
        {
            throw new WindupException("Failed to load support functions due to: " + e.getMessage(), e);
        }

        Map<String, ?> supportFunctions = (Map<String, ?>) binding.getVariable("supportFunctions");
        for (Map.Entry<String, ?> supportFunctionEntry : supportFunctions.entrySet())
        {
            binding.setVariable(supportFunctionEntry.getKey(), supportFunctionEntry.getValue());
        }
        binding.setVariable("supportFunctions", null);

        String scriptPath = null;
        for (URL resource : getScripts(context))
        {
            try (Reader reader = new InputStreamReader(resource.openStream()))
            {
                List<AbstractRuleProvider> ruleProviders = new ArrayList<>();
                binding.setVariable("windupRuleProviderBuilders", ruleProviders);

                scriptPath = resource.toExternalForm();
                binding.setVariable(CURRENT_WINDUP_SCRIPT, scriptPath);
                shell.evaluate(reader);

                List<AbstractRuleProvider> providers = (List<AbstractRuleProvider>) binding.getVariable("windupRuleProviderBuilders");
                for (AbstractRuleProvider provider : providers)
                {
                    if (provider instanceof RuleProviderBuilder)
                    {
                        ((RuleProviderBuilder) provider).setOrigin(scriptPath);
                    }
                    results.add(provider);
                }
            }
            catch (Exception e)
            {
                throw new WindupException("Failed to evaluate configuration from script [" + scriptPath + "]: ", e);
            }
        }

        return results;
    }

    private ClassLoader getCompositeClassloader()
    {
        List<ClassLoader> loaders = new ArrayList<>();
        AddonFilter filter = new AddonFilter()
        {
            @Override
            public boolean accept(Addon addon)
            {
                Set<AddonDependency> dependencies = addon.getDependencies();
                for (AddonDependency dependency : dependencies)
                {
                    // TODO this should only accept addons that depend on windup-config-groovy or whatever we call that
                    if (dependency.getDependency().getId().getName().contains("groovy"))
                    {
                        return true;
                    }
                }
                return false;

            }
        };

        for (Addon addon : furnace.getAddonRegistry().getAddons(filter))
        {
            loaders.add(addon.getClassLoader());
        }

        return new FurnaceCompositeClassLoader(getClass().getClassLoader(), loaders);
    }

    private Iterable<URL> getScripts(GraphContext context)
    {
        List<URL> results = new ArrayList<>();
        List<URL> scripts = scanner.scan(GROOVY_RULES_EXTENSION);
        results.addAll(scripts);

        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(context);
        Iterable<FileModel> userRulesResourceModels = cfg.getUserRulesPaths();
        for (ResourceModel fm : userRulesResourceModels)
        {
            results.addAll(getScripts(fm));
        }
        return results;
    }

    private Collection<URL> getScripts(ResourceModel userRulesResourceModel)
    {
        String userRulesDirectory = userRulesResourceModel == null ? null : userRulesResourceModel.getFilePath();

        Path userRulesPath = Paths.get(userRulesDirectory);

        if (!Files.isDirectory(userRulesPath))
        {
            LOG.warning("Not scanning: " + userRulesPath.normalize().toString() + " for rules as the directory could not be found!");
            return Collections.emptyList();
        }
        if (!Files.isDirectory(userRulesPath))
        {
            LOG.warning("Not scanning: " + userRulesPath.normalize().toString() + " for rules as the directory could not be read!");
            return Collections.emptyList();
        }

        final List<URL> results = new ArrayList<>();
        try
        {
            Files.walkFileTree(userRulesPath, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    if (file.getFileName().toString().endsWith(GROOVY_RULES_EXTENSION))
                    {
                        results.add(file.toUri().toURL());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to search userdir: \"" + userRulesPath + "\" for groovy rules due to: "
                        + e.getMessage(), e);
        }

        return results;
    }
}
