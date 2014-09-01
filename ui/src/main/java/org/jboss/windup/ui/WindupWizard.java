package org.jboss.windup.ui;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
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
import org.jboss.forge.addon.ui.progress.UIProgressMonitor;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizard;
import org.jboss.windup.engine.WindupProcessorConfig;
import org.jboss.windup.engine.WindupProgressMonitor;
import org.jboss.windup.graph.model.WindupConfigurationModel;

/**
 * Provides a basic forge UI for running windup from within the Forge shell.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class WindupWizard implements UIWizard, UICommand
{
    @Inject
    private WindupService windupService;

    @Inject
    @WithAttributes(label = "Input", required = true, description = "Input File or Directory (a Directory is required for source mode)")
    private UIInput<FileResource<?>> input;

    @Inject
    @WithAttributes(label = "Output", required = true, description = "Output Directory (WARNING: any existing files will be removed)")
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
    @WithAttributes(label = "Overwrite", required = false, defaultValue = "false", description = "Force overwrite of the output directory, without prompting")
    private UIInput<Boolean> overwrite;

    @Inject
    @WithAttributes(label = "User Rules Directory", required = false, description = "User Rules Directory (Search pattern: *.windup.groovy)")
    private UIInput<DirectoryResource> userRulesDirectory;

    @Override
    public UICommandMetadata getMetadata(UIContext ctx)
    {
        return Metadata.forCommand(getClass()).name("Windup Migrate App").description("Run Windup Migration Analyzer")
                    .category(Categories.create("Platform", "Migration"));
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception
    {
        builder.add(input).add(output).add(packages).add(excludePackages).add(fetchRemote).add(sourceMode)
                    .add(overwrite).add(userRulesDirectory);
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception
    {

        File inputFile = this.input.getValue().getUnderlyingResourceObject();
        File outputFile = this.output.getValue().getUnderlyingResourceObject();
        FileResource<DirectoryResource> userRulesInputValue = this.userRulesDirectory.getValue();
        File userRulesDirectory = userRulesInputValue != null ? userRulesInputValue.getUnderlyingResourceObject()
                    : null;
        List<String> scanJavaPackages = (List<String>) this.packages.getValue();
        List<String> excludeJavaPackages = (List<String>) this.excludePackages.getValue();
        boolean fetchRemote = this.fetchRemote.getValue();
        boolean sourceMode = this.sourceMode.getValue();
        boolean overwrite = this.overwrite.getValue();
        if (!overwrite && pathNotEmpty(outputFile))
        {
            String promptMsg = "Overwrite all contents of \"" + outputFile.toString()
                        + "\" (anything already in the directory will be deleted)?";
            if (!context.getPrompt().promptBoolean(promptMsg))
            {
                return Results.fail("Windup execution aborted!");
            }
        }

        FileUtils.deleteDirectory(outputFile);

        WindupConfigurationModel cfg = windupService.createServiceConfiguration();
        cfg.setInputPath(inputFile.getAbsolutePath());
        cfg.setOutputPath(outputFile.getAbsolutePath());
        cfg.setFetchRemoteResources(fetchRemote);
        cfg.setSourceMode(sourceMode);
        cfg.setScanJavaPackageList(scanJavaPackages);
        cfg.setExcludeJavaPackageList(excludeJavaPackages);
        if (userRulesDirectory != null)
        {
            cfg.setUserRulesPath(userRulesDirectory.getAbsolutePath());
        }

        UIProgressMonitor uiProgressMonitor = context.getProgressMonitor();
        WindupProgressMonitor progressMonitor = new WindupProgressMonitorAdapter(uiProgressMonitor);
        WindupProcessorConfig wpConf = new WindupProcessorConfig().setProgressMonitor(progressMonitor).setOutputDirectory(outputFile.toPath());
        windupService.execute(wpConf);

        uiProgressMonitor.done();

        return Results.success("Windup execution successful!");
    }

    private boolean pathNotEmpty(File f)
    {
        if (f.exists() && !f.isDirectory())
        {
            return true;
        }
        if (f.isDirectory() && f.listFiles() != null && f.listFiles().length > 0)
        {
            return true;
        }
        return false;
    }

    @Override
    public void validate(UIValidationContext context)
    {
        FileResource<?> inputValue = this.input.getValue();
        if (inputValue == null)
        {
            context.addValidationError(this.input, "Input path not specified");
            return;
        }

        File inputFile = inputValue.getUnderlyingResourceObject();
        if (inputFile == null || !inputFile.exists())
        {
            context.addValidationError(this.input, "Input path does not exist");
        }

        FileResource<DirectoryResource> userRulesInputValue = this.userRulesDirectory.getValue();
        if (userRulesInputValue != null && !userRulesInputValue.getUnderlyingResourceObject().isDirectory())
        {
            context.addValidationError(this.userRulesDirectory, "User Rules Directory must exist");
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
