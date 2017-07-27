package org.jboss.windup.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.resource.util.ResourcePathResolver;
import org.jboss.forge.addon.ui.UIProvider;
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
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.addon.ui.progress.UIProgressMonitor;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.validate.UIValidator;
import org.jboss.windup.config.ConfigurationOption;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.exec.configuration.options.OutputPathOption;
import org.jboss.windup.exec.configuration.options.OverwriteOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.util.Util;

/**
 * Provides a basic Forge UI implementation for running Windup from within a {@link UIProvider}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, ozizka at seznam.cz</a>
 */
public class WindupCommand implements UICommand
{
    private Map<ConfigurationOption, InputComponent<?, ?>> inputOptions = new LinkedHashMap<>();

    @Inject
    private InputComponentFactory componentFactory;

    @Inject
    private GraphContextFactory graphContextFactory;

    @Inject
    private WindupProcessor processor;

    @Inject
    private ResourceFactory resourceFactory;

    /*
     * Using a Set because messages seem to be added multiple times by validation. Need to look into this.
     */
    private Set<ValidationResult> promptMessages = new HashSet<>();

    @Override
    public UICommandMetadata getMetadata(UIContext ctx)
    {
        return Metadata.forCommand(getClass()).name(Util.WINDUP_BRAND_NAME_LONG).description("Run "+ Util.WINDUP_BRAND_NAME_LONG)
                    .category(Categories.create("Platform", "Migration"));
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void initializeUI(UIBuilder builder) throws Exception
    {
        initializeConfigurationOptionComponents(builder);

        final UIInputMany inputPaths = (UIInputMany) getInputForOption(InputPathOption.class);
        final UIInput outputPath = (UIInput) getInputForOption(OutputPathOption.class);
        outputPath.setDefaultValue(new Callable<DirectoryResource>()
        {
            @Override
            public DirectoryResource call() throws Exception
            {
                Iterable<File> inputPathsIterable = inputPaths.getValue();

                if (inputPathsIterable.iterator().hasNext())
                {
                    // set the default based on the first one

                    File value = inputPathsIterable.iterator().next();
                    File childDirectory = new File(value.getCanonicalFile().getParentFile(), value.getName() + ".report");

                    return resourceFactory.create(DirectoryResource.class, childDirectory);
                }
                return null;
            }

        });

        outputPath.addValidator(new UIValidator()
        {
            @Override
            public void validate(UIValidationContext context)
            {
                File outputFile = (File) getValueForInput(getOption(OutputPathOption.NAME), outputPath);
                final UIInputMany inputPathsUI = (UIInputMany) getInputForOption(InputPathOption.class);
                Iterable<File> inputPathsIterable = inputPathsUI.getValue();

                /**
                 * It would be really nice to be able to use native Resource types here... but we can't "realllly"
                 * do that because the Windup configuration API doesn't understand Forge data types, so instead we
                 * use string comparison and write a test case.
                 */
                //File inputFile = (File) inputPath.getUnderlyingResourceObject();
                for (File inputFile : inputPathsIterable){
                    if (inputFile.equals(outputFile))
                        context.addValidationError(outputPath, "Output file cannot be the same as the input file.");

                    File inputParent = inputFile.getParentFile();
                    while (inputParent != null)
                    {
                        if (inputParent.equals(outputFile))
                            context.addValidationError(outputPath, "Output path must not be a parent of input path.");
                        inputParent = inputParent.getParentFile();
                    }

                    File outputParent = outputFile.getParentFile();
                    while (outputParent != null)
                    {
                        if (outputParent.equals(inputFile))
                            context.addValidationError(inputPathsUI, "Input path must not be a parent of output path.");
                        outputParent = outputParent.getParentFile();
                    }
                }
            }
        });
    }

    @Override
    public void validate(UIValidationContext context)
    {
        for (Entry<ConfigurationOption, InputComponent<?, ?>> entry : this.inputOptions.entrySet())
        {
            final InputComponent<?, ?> inputComponent = entry.getValue();

            ConfigurationOption option = entry.getKey();
            Object inputValue = getValueForInput(option, inputComponent);
            if (inputValue == null && !option.isRequired())
                return;

            ValidationResult result = option.validate(inputValue);

            switch (result.getLevel())
            {
                case ERROR:
                    context.addValidationError(inputComponent, result.getMessage());
                    break;
                case WARNING:
                    context.addValidationWarning(inputComponent, result.getMessage());
                    break;
                case PROMPT_TO_CONTINUE:
                    this.promptMessages.add(result);
                    break;
            }
        }
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception
    {
        if (!this.promptMessages.isEmpty())
        {
            for (ValidationResult message : promptMessages)
            {
                UIOutput output = context.getUIContext().getProvider().getOutput();
                output.warn(output.out(), message.getMessage());
            }

            if (context.getPrompt().promptBoolean("Would you like to continue?", true) == false)
            {
                return Results.fail("Aborted by the user.");
            }
        }

        WindupConfiguration windupConfiguration = new WindupConfiguration();
        for (Entry<ConfigurationOption, InputComponent<?, ?>> entry : this.inputOptions.entrySet())
        {
            ConfigurationOption option = entry.getKey();
            String name = option.getName();
            Object value = getValueForInput(option, entry.getValue());
            windupConfiguration.setOptionValue(name, value);
        }

        windupConfiguration.useDefaultDirectories();

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

        /*
         * Put this in the context for debugging, and unit tests (or anything else that needs it).
         */
        context.getUIContext().getAttributeMap().put(WindupConfiguration.class, windupConfiguration);

        FileUtils.deleteQuietly(windupConfiguration.getOutputDirectory().toFile());
        Path graphPath = windupConfiguration.getOutputDirectory().resolve("graph");
        try (GraphContext graphContext = graphContextFactory.create(graphPath))
        {
            context.getUIContext().getAttributeMap().put(GraphContext.class, graphContext);
            UIProgressMonitor uiProgressMonitor = context.getProgressMonitor();
            WindupProgressMonitor progressMonitor = new WindupProgressMonitorAdapter(uiProgressMonitor);
            windupConfiguration
                        .setProgressMonitor(progressMonitor)
                        .setGraphContext(graphContext);
            processor.execute(windupConfiguration);

            uiProgressMonitor.done();

            Path indexHtmlPath = windupConfiguration.getOutputDirectory().resolve("index.html").normalize().toAbsolutePath();
            return Results.success("Report created: " + indexHtmlPath + System.getProperty("line.separator")
                        + "              Access it at this URL: " + indexHtmlPath.toUri());
        }
    }

    @Override
    public boolean isEnabled(UIContext context)
    {
        return true;
    }

    /*
     * Utility methods
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initializeConfigurationOptionComponents(UIBuilder builder)
    {
        for (final ConfigurationOption option : WindupConfiguration.getWindupConfigurationOptions())
        {
            InputComponent<?, ?> inputComponent = null;
            switch (option.getUIType())
            {
            case SINGLE:
            {
                UIInput<?> inputSingle = componentFactory.createInput(option.getName(), option.getType());
                inputSingle.setDefaultValue(new DefaultValueAdapter(option));
                inputComponent = inputSingle;
                break;
            }
            case MANY:
            {
                // forge can't handle "Path", so use File
                Class<?> optionType = option.getType() == Path.class ? File.class : option.getType();

                UIInputMany<?> inputMany = componentFactory.createInputMany(option.getName(), optionType);
                inputMany.setDefaultValue(new DefaultValueAdapter(option, Iterable.class));
                inputComponent = inputMany;
                break;
            }
            case SELECT_MANY:
            {
                UISelectMany<?> selectMany = componentFactory.createSelectMany(option.getName(), option.getType());
                selectMany.setValueChoices((Iterable) option.getAvailableValues());
                selectMany.setDefaultValue(new DefaultValueAdapter(option, Iterable.class));
                inputComponent = selectMany;
                break;
            }
            case SELECT_ONE:
            {
                UISelectOne<?> selectOne = componentFactory.createSelectOne(option.getName(), option.getType());
                selectOne.setValueChoices((Iterable) option.getAvailableValues());
                selectOne.setDefaultValue(new DefaultValueAdapter(option));
                inputComponent = selectOne;
                break;
            }
            case DIRECTORY:
            {
                UIInput<DirectoryResource> directoryInput = componentFactory.createInput(option.getName(),
                            DirectoryResource.class);
                directoryInput.setDefaultValue(new DefaultValueAdapter(option, DirectoryResource.class));
                inputComponent = directoryInput;
                break;
            }
            case FILE:
            {
                UIInput<?> fileInput = componentFactory.createInput(option.getName(), FileResource.class);
                fileInput.setDefaultValue(new DefaultValueAdapter(option, FileResource.class));
                inputComponent = fileInput;
                break;
            }
            case FILE_OR_DIRECTORY:
            {
                UIInput<?> fileOrDirInput = componentFactory.createInput(option.getName(), FileResource.class);
                fileOrDirInput.setDefaultValue(new DefaultValueAdapter(option, FileResource.class));
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
            inputOptions.put(option, inputComponent);
        }
    }

    private ConfigurationOption getOption(String name)
    {
        for (Entry<ConfigurationOption, InputComponent<?, ?>> entry : this.inputOptions.entrySet())
        {
            if (StringUtils.equals(name, entry.getKey().getName()))
            {
                return entry.getKey();
            }
        }
        return null;
    }

    private InputComponent<?, ?> getInputForOption(Class<? extends ConfigurationOption> option)
    {
        for (Entry<ConfigurationOption, InputComponent<?, ?>> entry : this.inputOptions.entrySet())
        {
            if (option.isAssignableFrom(entry.getKey().getClass()))
            {
                return entry.getValue();
            }
        }
        return null;
    }

    private Object getValueForInput(ConfigurationOption option, InputComponent<?, ?> input)
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

        if (option.getType() == Path.class)
        {
            // these have to be converted
            Set<Path> paths = new LinkedHashSet<>();
            if (value instanceof List)
            {
                for (Object path : (List) value)
                {
                    if (path instanceof File)
                        path = ((File) path).toPath();
                    paths.add((Path) path);
                }
            }
            value = paths;
        }

        return value;
    }

    private Resource<?> getResourceResolved(Resource<?> value)
    {
        Resource<?> resource = (Resource<?>) value;
        File file = (File) resource.getUnderlyingResourceObject();
        return new ResourcePathResolver(resourceFactory, resource, file.getPath()).resolve().get(0);
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

}
