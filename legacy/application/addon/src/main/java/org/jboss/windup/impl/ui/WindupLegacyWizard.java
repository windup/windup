package org.jboss.windup.impl.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.forge.addon.configuration.Configuration;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIInputMany;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizard;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.windup.WindupEnvironment;
import org.jboss.windup.WindupLegacyService;
import org.jboss.windup.util.SharedProperties;

public class WindupLegacyWizard implements UIWizard, UICommand
{
    private static final String KEY_SUPPLEMENTAL_RULES_DIRECTORY = "WindupWizard.supplementalRulesDirectory";
    
    private static Logger log = Logger.getLogger(WindupLegacyWizard.class.getName());

    @Inject
    private WindupLegacyService windup;

    @Inject
    private AddonRegistry registry;

    @Inject
    private Configuration configuration;
    
    @Inject
    @WithAttributes(label = "Input", required = true, description = "Input File or Directory (a Directory is required for source mode)")
    private UIInput<FileResource<?>> input;

    @Inject
    @WithAttributes(label = "Output", required = true, description = "Output Directory")
    private UIInput<DirectoryResource> output;

    @Inject
    @WithAttributes(label = "Scan Java Packages", required = true, description = "A list of java package name prefixes to scan (eg, com.myapp)")
    private UIInputMany<String> packages;

    @Inject
    @WithAttributes(label = "Exclude Java Packages", required = false, description = "A list of java package name prefixes to exclude (eg, com.myapp.subpackage)")
    private UIInputMany<String> excludePackages;

    @Inject
    @WithAttributes(label = "Fetch Remote Resources", required = false, defaultValue = "true", description = "Indicates whether to fetch maven information from the internet (default: false)")
    private UIInput<Boolean> fetchRemote;

    @Inject
    @WithAttributes(label = "Source Mode", required = false, defaultValue = "false", description = "Indicates whether the input file or directory is a source code or compiled binaries (Default: Compiled)")
    private UIInput<Boolean> sourceMode;

    @Inject
    @WithAttributes(label = "Target Platform", required = false, description = "Target Platform to migrate to")
    private UIInput<String> targetPlatform;
    
    @Inject
    @WithAttributes(label = "Exclude built-in rules", required = false, description = "Exclude the builtin rules from being processed", defaultValue = "false")
    private UIInput<Boolean> excludeBuiltinRules;
    
    @Inject
    @WithAttributes(label = "Supplemental Rules Folder", required = false, description = "Directory containing additional rules (note: rule filenames must match the pattern *.windup.xml)")
    private UIInput<DirectoryResource> supplementalRulesFolder;

    @Override
    public NavigationResult next(UINavigationContext context) throws Exception
    {
        return null;
    }

    @Override
    public UICommandMetadata getMetadata(UIContext ctx)
    {
        return Metadata.forCommand(getClass()).name("Windup 1.x Migrate App").description("Run Windup 1.x Migration Analyzer")
                    .category(Categories.create("Platform", "Migration"));
    }

    @Override
    public boolean isEnabled(UIContext context)
    {
        return true;
    }

    @Override
    public void initializeUI(final UIBuilder builder) throws Exception
    {
        builder.add(input).add(output).add(packages).add(excludePackages).add(fetchRemote).add(sourceMode)
                    .add(targetPlatform).add(supplementalRulesFolder).add(excludeBuiltinRules);
    }

    @Override
    public void validate(UIValidationContext context)
    {
    }

    private File getUserProvidedRulesFolder() {
        if (supplementalRulesFolder.getValue() != null) {
            File userProvidedRulesFolderFile = supplementalRulesFolder.getValue().getUnderlyingResourceObject();
            configuration.setProperty(KEY_SUPPLEMENTAL_RULES_DIRECTORY, userProvidedRulesFolderFile.getAbsolutePath());
            return userProvidedRulesFolderFile;
        } else {
            String userProvidedRulesFolder = configuration.getString(KEY_SUPPLEMENTAL_RULES_DIRECTORY);
            if (StringUtils.isEmpty(userProvidedRulesFolder)) {
                Path windupConfigHomePath = SharedProperties.getWindupConfigurationDirectory();
                return windupConfigHomePath.resolve(".windup").resolve("rules").toFile();
            } else {
                return new File(userProvidedRulesFolder);
            }
        }
    }
    
    @Override
    public Result execute(UIExecutionContext executionContext) throws Exception
    {
        StringBuilder builder = new StringBuilder();
        for (String packg : packages.getValue())
        {
            builder.append(packg).append(" ");
        }
        try
        {
            WindupEnvironment options = new WindupEnvironment();
            options.setInputPath(input.getValue().getUnderlyingResourceObject());
            options.setOutputPath(output.getValue().getUnderlyingResourceObject());
            options.setIncludeJavaPackageSignature((List<String>) packages.getValue());
            
            File userProvidedRulesDirectory = getUserProvidedRulesFolder();
            options.setSupplementalRulesDirectory(userProvidedRulesDirectory);
            options.setExcludeBuiltinRules(excludeBuiltinRules.getValue());

            options.setFetchRemote(fetchRemote.getValue());
            options.setExcludeJavaPackageSignature((List<String>) excludePackages.getValue());
            options.setSource(sourceMode.getValue());
            options.setTargetPlatform(targetPlatform.getValue());

            windup.execute(options);
            return Results.success();
        }
        catch (Exception ex)
        {
            final String msg = "Could not run Windup: " + ex.getMessage();
            log.log(Level.SEVERE, msg, ex);
            return Results.fail(msg, ex);
        }
    }

}
