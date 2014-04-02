package org.jboss.windup.impl.ui;

import java.util.List;
import java.util.logging.Level;
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
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.windup.WindupEnvironment;
import org.jboss.windup.WindupService;

public class WindupWizard implements UIWizard, UICommand
{
    private static Logger log = Logger.getLogger(WindupWizard.class.getName());

    @Inject
    private WindupService windup;

    @Inject
    private AddonRegistry registry;

    @Inject
    @WithAttributes(label = "Input", required = true)
    private UIInput<FileResource<?>> input;

    @Inject
    @WithAttributes(label = "Output", required = true)
    private UIInput<DirectoryResource> output;

    @Inject
    @WithAttributes(label = "Scan Java Packages", required = true)
    private UIInputMany<String> packages;

    @Inject
    @WithAttributes(label = "Exclude Java Packages", required = false)
    private UIInputMany<String> excludePackages;

    @Inject
    @WithAttributes(label = "Fetch Remote Resources", required = false, defaultValue = "true")
    private UIInput<Boolean> fetchRemote;

    @Inject
    @WithAttributes(label = "Source Mode", required = false, defaultValue = "false")
    private UIInput<Boolean> sourceMode;

    @Inject
    @WithAttributes(label = "Target Platform", required = false)
    private UIInput<String> targetPlatform;

    @Override
    public NavigationResult next(UINavigationContext context) throws Exception
    {
        return null;
    }

    @Override
    public UICommandMetadata getMetadata(UIContext ctx)
    {
        return Metadata.forCommand(getClass()).name("Run Windup 1.x").description("Run Windup 1.x Migration Analyzer")
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
                    .add(targetPlatform);
    }

    @Override
    public void validate(UIValidationContext context)
    {
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

            options.setFetchRemote(fetchRemote.getValue().booleanValue());
            options.setExcludeJavaPackageSignature((List<String>) excludePackages.getValue());
            options.setSource(sourceMode.getValue().booleanValue());
            options.setTargetPlatform(targetPlatform.getValue());

            windup.execute(options);
            return Results.success();
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Could not run Windup", e);
            return Results.fail("Could not run Windup", e);
        }
    }

}
