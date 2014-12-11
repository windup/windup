package org.jboss.windup.ui;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.resource.util.ResourcePathResolver;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.input.InputComponentFactory;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIInputMany;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.progress.UIProgressMonitor;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.config.WindupConfigurationOption;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.exec.configuration.options.OutputPathOption;
import org.jboss.windup.exec.configuration.options.OverwriteOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.util.WindupPathUtil;

/**
 * Provides a basic forge UI for running windup from within the Forge shell.
 *
 * @author jsightler <jesse.sightler@gmail.com>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 *
 */
public class WindupCommand implements UICommand
{
    public static final String WINDUP_CONFIGURATION = "windupConfiguration";

    @Inject
    private InputComponentFactory componentFactory;

    @Inject
    private GraphContextFactory graphContextFactory;

    @Inject
    private WindupProcessor processor;

    @Inject
    private ResourceFactory resourceFactory;

    private List<WindupOptionAndInput> inputOptions = new ArrayList<>();

    @Override
    public UICommandMetadata getMetadata(UIContext ctx)
    {
        return Metadata.forCommand(getClass()).name("Windup Migrate App").description("Run Windup Migration Analyzer")
                    .category(Categories.create("Platform", "Migration"));
    }

    // Used below for default values.
    private static class DefaultValue implements Callable
    {
        private WindupConfigurationOption option;
        private Class<?> expectedType;

        public DefaultValue(WindupConfigurationOption option, Class<?> expectedType)
        {
            this.option = option;
            this.expectedType = expectedType;
        }

        public DefaultValue(WindupConfigurationOption option)
        {
            this(option, null);
        }

        public Object call() throws Exception
        {
            Object val = this.option.getDefaultValue();
            if (val != null && this.expectedType != null && !this.expectedType.isAssignableFrom(val.getClass()))
                throw new IllegalStateException("Windup option " + option.getName() +
                            " was expected to return " + expectedType.getName() + " but returned " + val.getClass());
            return val;
        }
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception
    {
        for (final WindupConfigurationOption option : WindupConfiguration.getWindupConfigurationOptions())
        {
            InputComponent<?, ?> inputComponent = null;
            switch (option.getUIType())
            {
            case SINGLE:
            {
                UIInput<?> inputSingle = componentFactory.createInput(option.getName(), option.getType());
                inputSingle.setDefaultValue(new DefaultValue(option));
                inputComponent = inputSingle;
                break;
            }
            case MANY:
            {
                UIInputMany<?> inputMany = componentFactory.createInputMany(option.getName(), option.getType());
                inputMany.setDefaultValue(new DefaultValue(option, Iterable.class));
                inputComponent = inputMany;
                break;
            }
            case SELECT_MANY:
            {
                UISelectMany<?> selectMany = componentFactory.createSelectMany(option.getName(), option.getType());
                selectMany.setDefaultValue(new DefaultValue(option, Iterable.class));
                inputComponent = selectMany;
                break;
            }
            case SELECT_ONE:
            {
                UISelectOne<?> selectOne = componentFactory.createSelectOne(option.getName(), option.getType());
                selectOne.setDefaultValue(new DefaultValue(option));
                inputComponent = selectOne;
                break;
            }
            case DIRECTORY:
            {
                UIInput<DirectoryResource> directoryInput = componentFactory.createInput(option.getName(),
                            DirectoryResource.class);
                directoryInput.setDefaultValue(new DefaultValue(option, DirectoryResource.class));
                inputComponent = directoryInput;
                break;
            }
            case FILE:
            {
                UIInput<?> fileInput = componentFactory.createInput(option.getName(), FileResource.class);
                fileInput.setDefaultValue(new DefaultValue(option, FileResource.class));
                inputComponent = fileInput;
                break;
            }
            case FILE_OR_DIRECTORY:
            {
                UIInput<?> fileOrDirInput = componentFactory.createInput(option.getName(), FileResource.class);
                fileOrDirInput.setDefaultValue(new DefaultValue(option, FileResource.class));
                inputComponent = fileOrDirInput;
                break;
            }
            }
            if (inputComponent == null)
            {
                throw new IllegalArgumentException("Could not build input component for: " + option);
            }
            inputComponent.setLabel(option.getLabel());
            inputComponent.setRequired(option.isRequired());
            inputComponent.setDescription(option.getDescription());
            builder.add(inputComponent);
            inputOptions.add(new WindupOptionAndInput(option, inputComponent));
        }
        final UIInput inputForInput = (UIInput) getInputForOption(InputPathOption.class);
        UIInput inputForOutput = (UIInput) getInputForOption(OutputPathOption.class);
        inputForOutput.setDefaultValue(new Callable<DirectoryResource>()
        {

            @Override
            public DirectoryResource call() throws Exception
            {
                if (inputForInput.getValue() != null)
                {
                    FileResource value = (FileResource) inputForInput.getValue();
                    DirectoryResource childDirectory = value.getParent().getChildDirectory(value.getName() + ".report");
                    return childDirectory;
                }

                return null;
            }

        });
    }

    private InputComponent<?, ?> getInputForOption(Class<? extends WindupConfigurationOption> option)
    {
        for (WindupOptionAndInput pair : this.inputOptions)
        {
            if (option.isAssignableFrom(pair.option.getClass()))
            {
                return pair.input;
            }
        }
        return null;
    }

    @Override
    public void validate(UIValidationContext context)
    {
        for (WindupOptionAndInput pair : this.inputOptions)
        {
            Object value = getValueForInput(pair.input);
            ValidationResult result = pair.option.validate(value);
            if (!result.isSuccess())
            {
                context.addValidationError(pair.input, result.getMessage());
            }
        }

    }

    private Object getValueForInput(InputComponent<?, ?> input)
    {
        Object value = input.getValue();
        if (value == null)
        {
            return value;
        }

        if (value instanceof Resource<?>)
        {
            Resource<?> resourceResolved = getResourceResolved((Resource<?>) value);
            return resourceResolved.getUnderlyingResourceObject();
        }
        return value;
    }

    private Resource<?> getResourceResolved(Resource<?> value)
    {
        Resource<?> resource = (Resource<?>) value;
        File file = (File) resource.getUnderlyingResourceObject();
        return new ResourcePathResolver(resourceFactory, resource, file.getPath()).resolve().get(0);
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception
    {
        WindupConfiguration windupConfiguration = new WindupConfiguration();
        for (WindupOptionAndInput pair : this.inputOptions)
        {
            String key = pair.option.getName();
            Object value = getValueForInput(pair.input);
            windupConfiguration.setOptionValue(key, value);
        }

        // add dist/rules/ and ${forge.home}/rules/ to the user rules directory list
        Path userRulesDir = WindupPathUtil.getWindupUserRulesDir();
        if (!Files.isDirectory(userRulesDir))
        {
            Files.createDirectories(userRulesDir);
        }
        windupConfiguration.addDefaultUserRulesDirectory(userRulesDir);

        Path userIgnoreDir = WindupPathUtil.getWindupIgnoreListDir();
        if (!Files.isDirectory(userIgnoreDir))
        {
            Files.createDirectories(userIgnoreDir);
        }
        windupConfiguration.addDefaultUserIgnorePath(userIgnoreDir);

        Path windupHomeRulesDir = WindupPathUtil.getWindupHomeRules();
        if (!Files.isDirectory(windupHomeRulesDir))
        {
            Files.createDirectories(windupHomeRulesDir);
        }
        windupConfiguration.addDefaultUserRulesDirectory(windupHomeRulesDir);

        Path windupHomeIgnoreDir = WindupPathUtil.getWindupHomeIgnoreListDir();
        if (!Files.isDirectory(windupHomeIgnoreDir))
        {
            Files.createDirectories(windupHomeIgnoreDir);
        }
        windupConfiguration.addDefaultUserIgnorePath(windupHomeIgnoreDir);

        Boolean overwrite = (Boolean) windupConfiguration.getOptionMap().get(OverwriteOption.NAME);
        if (overwrite == null)
        {
            overwrite = false;
        }
        if (!overwrite && pathNotEmpty(windupConfiguration.getOutputDirectory().toFile()))
        {
            String promptMsg = "Overwrite all contents of \"" + windupConfiguration.getOutputDirectory().toString()
                        + "\" (anything already in the directory will be deleted)?";
            if (!context.getPrompt().promptBoolean(promptMsg, false))
            {
                String outputPath = windupConfiguration.getOutputDirectory().toString();
                return Results.fail("Files exist in " + outputPath + ", but --overwrite not specified. Aborting!");
            }
        }

        // put this in the context for debugging, and unit tests (or anything else that needs it)
        context.getUIContext().getAttributeMap().put(WindupConfiguration.class, windupConfiguration);

        FileUtils.deleteQuietly(windupConfiguration.getOutputDirectory().toFile());
        Path graphPath = windupConfiguration.getOutputDirectory().resolve("graph");
        try (GraphContext graphContext = graphContextFactory.create(graphPath))
        {
            UIProgressMonitor uiProgressMonitor = context.getProgressMonitor();
            WindupProgressMonitor progressMonitor = new WindupProgressMonitorAdapter(uiProgressMonitor);
            windupConfiguration
                        .setProgressMonitor(progressMonitor)
                        .setGraphContext(graphContext);
            processor.execute(windupConfiguration);

            uiProgressMonitor.done();

            return Results.success("Windup report created: " + windupConfiguration.getOutputDirectory().toAbsolutePath() + "/index.html");
        }
    }

    @Override
    public boolean isEnabled(UIContext context)
    {
        return true;
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

    private class WindupOptionAndInput
    {
        private WindupConfigurationOption option;
        private InputComponent<?, ?> input;

        public WindupOptionAndInput(WindupConfigurationOption option, InputComponent<?, ?> input)
        {
            this.option = option;
            this.input = input;
        }
    }
}
