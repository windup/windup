package org.jboss.windup.ui;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

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
import org.jboss.windup.graph.model.WindupConfigurationModel;

public class WindupWizard implements UIWizard, UICommand
{
    private static Logger log = Logger.getLogger(WindupWizard.class.getName());

    @Inject
    private WindupService windupService;

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

    @Override
    public UICommandMetadata getMetadata(UIContext ctx)
    {
        return Metadata.forCommand(getClass()).name("Windup Migrate App").description("Run Windup Migration Analyzer")
                    .category(Categories.create("Platform", "Migration"));
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception
    {
        builder.add(input).add(output).add(packages).add(excludePackages).add(fetchRemote).add(sourceMode);
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception
    {
        File inputFile = this.input.getValue().getUnderlyingResourceObject();
        File outputFile = this.output.getValue().getUnderlyingResourceObject();
        List<String> scanJavaPackages = (List<String>) this.packages.getValue();
        List<String> excludeJavaPackages = (List<String>) this.excludePackages.getValue();
        boolean fetchRemote = this.fetchRemote.getValue();
        boolean sourceMode = this.sourceMode.getValue();

        WindupConfigurationModel cfg = windupService.createServiceConfiguration();
        cfg.setInputPath(inputFile.getAbsolutePath());
        cfg.setOutputPath(outputFile.getAbsolutePath());
        cfg.setInputPath(inputFile.getAbsolutePath());
        cfg.setFetchRemoteResources(fetchRemote);
        cfg.setSourceMode(sourceMode);
        cfg.setScanJavaPackageList(scanJavaPackages);
        cfg.setExcludeJavaPackageList(excludeJavaPackages);

        windupService.execute(cfg);

        return Results.success("Windup execution successful!");
    }

    @Override
    public void validate(UIValidationContext context)
    {
        File inputFile = input.getValue().getUnderlyingResourceObject();
        if (inputFile == null || !inputFile.exists())
        {
            context.addValidationError(input, "Input path does not exist");
        }
        List<String> scanJavaPackages = (List<String>) this.packages.getValue();
        if (scanJavaPackages == null || scanJavaPackages.isEmpty())
        {
            context.addValidationError(this.packages, "Packages to scan must be specified");
        }
    }

    @Override
    public boolean isEnabled(UIContext context)
    {
        return true;
    }

    @Override
    public NavigationResult next(UINavigationContext context) throws Exception
    {
        return null;
    }
}
