package org.jboss.windup.bootstrap.commands.windup;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.janusgraph.core.JanusGraphException;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.windup.bootstrap.Bootstrap;
import org.jboss.windup.bootstrap.ConsoleProgressMonitor;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;
import org.jboss.windup.config.ConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.SkipReportsRenderingOption;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.config.metadata.RuleProviderRegistryCache;
import org.jboss.windup.exec.Util;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.ExplodedAppInputOption;
import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.exec.configuration.options.OutputPathOption;
import org.jboss.windup.exec.configuration.options.OverwriteOption;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.jboss.windup.exec.configuration.options.UserRulesDirectoryOption;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.rules.apps.java.config.ExcludePackagesOption;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.tattletale.DisableTattletaleReportOption;
import org.jboss.windup.rules.apps.tattletale.EnableTattletaleReportOption;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.util.exception.WindupException;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This is the interactive command-line user interface of Windup
 * which does basic validation of the input and then runs WindupProcessorImpl.
 */
public class RunWindupCommand implements Command, FurnaceDependent {
    private static final Logger log = Logging.get(RunWindupCommand.class);

    private Furnace furnace;
    private final List<String> arguments;
    private final AtomicBoolean batchMode;

    public RunWindupCommand(List<String> arguments, AtomicBoolean batchMode) {
        this.arguments = arguments;
        this.batchMode = batchMode;
    }

    @Override
    public CommandResult execute() {
        runWindup(arguments);
        return CommandResult.EXIT;
    }

    @Override
    public void setFurnace(Furnace furnace) {
        this.furnace = furnace;
    }

    @Override
    public CommandPhase getPhase() {
        return CommandPhase.EXECUTION;
    }

    @SuppressWarnings("unchecked")
    private void runWindup(List<String> arguments) {
        Iterable<ConfigurationOption> optionIterable = WindupConfiguration.getWindupConfigurationOptions(furnace);
        Map<String, ConfigurationOption> options = new HashMap<>();
        for (ConfigurationOption option : optionIterable)
            options.put(option.getName().toUpperCase(), option);

        Map<String, Object> optionValues = new HashMap<>();
        for (int i = 0; i < arguments.size(); i++) {
            String argument = arguments.get(i);
            String optionName = getOptionName(argument);
            if (optionName == null) {
                System.err.println("WARNING: Unrecognized command-line argument: " + argument);
                continue;
            }

            ConfigurationOption option = options.get(optionName.toUpperCase());
            if (option == null) {
                System.err.println("WARNING: Unrecognized command-line argument: " + argument);
                if (options.size() == 0) {
                    System.err.println("FATAL: Furnace Addon repository path: " + System.lineSeparator() + furnace.getAddonRegistry().toString());
                }
                continue;
            }

            // For MANY InputType, take the following arguments as values.
            if (option.getUIType() == InputType.MANY || option.getUIType() == InputType.SELECT_MANY) {
                List<Object> values = new ArrayList<>();
                i++;
                while (i < arguments.size()) {
                    final String arg = arguments.get(i);
                    final String name = getOptionName(arg);
                    if (name != null) {
                        // This is the next parameter... back up one and break the loop.
                        i--;
                        break;
                    }

                    String valueString = arguments.get(i);
                    // Lists are space delimited. split them here.
                    for (String value : StringUtils.split(valueString, ' '))
                        values.add(convertType(option.getType(), value));

                    i++;
                }

                /*
                 * This allows us to support specifying a parameter multiple times. For example: `windup --packages foo --packages bar --packages baz`
                 * While this is not necessarily the recommended approach, it would be nice for it to work smoothly if someone does it this way.
                 */
                if (optionValues.containsKey(option.getName()))
                    ((List<Object>) optionValues.get(option.getName())).addAll(values);
                else
                    optionValues.put(option.getName(), values);
            } else if (Boolean.class.isAssignableFrom(option.getType())) {
                optionValues.put(option.getName(), true);
            } else {
                String valueString = arguments.size() > (i + 1) ? arguments.get(++i) : null;

                if (getOptionName(valueString) != null) {
                    i--;
                    valueString = "";
                }

                Object value = convertType(option.getType(), valueString);
                optionValues.put(option.getName(), value);
            }
        }

        setDefaultOutputPath(optionValues);
        setDefaultOptionsValues(options, optionValues);

        RuleProviderRegistryCache ruleProviderRegistryCache = furnace.getAddonRegistry().getServices(RuleProviderRegistryCache.class).get();
        Iterable<File> userProvidedPaths = (Iterable<File>) optionValues.get(UserRulesDirectoryOption.NAME);
        if (userProvidedPaths != null) {
            for (File userProvidedPath : userProvidedPaths) {
                ruleProviderRegistryCache.addUserRulesPath(userProvidedPath.toPath());
            }
        }

        // Target - interactive
        Collection<String> targets = (Collection<String>) optionValues.get(TargetOption.NAME);
        if (targets != null && targets.contains("eap"))
        {
            System.err.println("ERROR: " + "Version must be specified for target 'eap' (for example, 'eap7' or 'eap8')");
            return;
        }

        if ((targets == null || targets.isEmpty()) && !batchMode.get())
        {
            String target = Bootstrap.promptForListItem("Please select a target:", ruleProviderRegistryCache.getAvailableTargetTechnologies(), "eap7");
            targets = Collections.singleton(target);
            optionValues.put(TargetOption.NAME, targets);
        }

        boolean validationSuccess = validateOptionValues(options, optionValues);
        if (!validationSuccess)
            return;

        boolean eapTarget = targets.stream().anyMatch(target -> target.startsWith("eap"));
        boolean disableReport = false;
        if (optionValues.containsKey(DisableTattletaleReportOption.NAME)) {
            disableReport = (Boolean) optionValues.get(DisableTattletaleReportOption.NAME);
        }
        boolean enableReport = false;
        if (optionValues.containsKey(EnableTattletaleReportOption.NAME)) {
            enableReport = (Boolean) optionValues.get(EnableTattletaleReportOption.NAME);
        }

        if (disableReport && enableReport) {
            System.out.println("INFO: --" + DisableTattletaleReportOption.NAME + " option ignored since --" + EnableTattletaleReportOption.NAME + " option has been provided as well.");
        } else if (eapTarget) {
            if (enableReport) {
                System.out.println("INFO: --" + EnableTattletaleReportOption.NAME + " option can be removed since Tattletale report generation is enabled by default when JBoss EAP is one of the analysis targets.");
            }
        } else {
            if (disableReport) {
                System.out.println("INFO: --" + DisableTattletaleReportOption.NAME + " option can be removed since Tattletale report generation is not enabled by default when JBoss EAP is not one of the analysis targets.");
            }
        }

        // In case of --unzippedAppInput or --sourceMode, treat the directories in --input as unzipped applications.
        // Otherwise, as a directory containing separate applications (default).
        boolean isExplodedApp = false;
        if (optionValues.containsKey(ExplodedAppInputOption.NAME)) {
            isExplodedApp = (Boolean) optionValues.get(ExplodedAppInputOption.NAME);
        }
        if (optionValues.containsKey(SourceModeOption.NAME)) {
            isExplodedApp = isExplodedApp || (Boolean) optionValues.get(SourceModeOption.NAME);
        }

        if (!isExplodedApp) {
            List<Path> input = (List<Path>) optionValues.get(InputPathOption.NAME);
            input = new InputsHandler().handle(input);
            optionValues.put(InputPathOption.NAME, input);
        }

        WindupConfiguration windupConfiguration = new WindupConfiguration();
        for (Map.Entry<String, ConfigurationOption> optionEntry : options.entrySet()) {
            ConfigurationOption option = optionEntry.getValue();
            windupConfiguration.setOptionValue(option.getName(), optionValues.get(option.getName()));
        }

        if (!validateInputAndOutputPath(windupConfiguration.getInputPaths(), windupConfiguration.getOutputDirectory()))
            return;

        try {
            windupConfiguration.useDefaultDirectories();
        } catch (IOException e) {
            System.err.println("ERROR: Failed to create default directories due to: " + e.getMessage());
            return;
        }

        Boolean overwrite = (Boolean) windupConfiguration.getOptionMap().get(OverwriteOption.NAME);
        if (overwrite == null) {
            overwrite = false;
        }

        if (!overwrite && pathNotEmpty(windupConfiguration.getOutputDirectory().toFile())) {
            String promptMsg = "Overwrite all contents of \"" + windupConfiguration.getOutputDirectory().toString()
                    + "\" (anything already in the directory will be deleted)?";
            if (!Bootstrap.prompt(promptMsg, false, batchMode.get())) {
                String outputPath = windupConfiguration.getOutputDirectory().toString();
                System.err.println("Files exist in " + outputPath + ", but --overwrite not specified. Aborting!");
                return;
            }
        }

        FileUtils.deleteQuietly(windupConfiguration.getOutputDirectory().toFile());
        Path graphPath = windupConfiguration.getOutputDirectory().resolve(GraphContextFactory.DEFAULT_GRAPH_SUBDIRECTORY);

        System.out.println();
        if (windupConfiguration.getInputPaths().size() == 1) {
            System.out.println("Input Application:" + windupConfiguration.getInputPaths().iterator().next());
        } else {
            System.out.println("Input Applications:");
            for (Path inputPath : windupConfiguration.getInputPaths()) {
                System.out.println("\t" + inputPath);
            }
            System.out.println();
        }
        System.out.println("Output Path:" + windupConfiguration.getOutputDirectory());
        System.out.println();

        normalizePackagePrefixes(windupConfiguration);

        try {
            WindupProgressMonitor progressMonitor = new ConsoleProgressMonitor();
            windupConfiguration.setProgressMonitor(progressMonitor);
            // Run Windup
            getWindupProcessor().execute(windupConfiguration);

            final Boolean skipReports = (Boolean) windupConfiguration.getOptionMap().get(SkipReportsRenderingOption.NAME);
            if (!skipReports) {
                Path indexHtmlPath = windupConfiguration.getOutputDirectory().resolve("index.html").normalize().toAbsolutePath();
                System.out.println("Report created: " + indexHtmlPath + System.getProperty("line.separator")
                        + "              Access it at this URL: " + indexHtmlPath.toUri());
            } else {
                System.out.println("Generating reports were disabled by option --skipReports");
                System.out.println("If using that option was unintentional, please run Windup again to generate reports.");
            }
        } catch (Exception e) {
            // Due to different classloaders involved in loading these exceptions,
            // the comparison must be based on exceptions' fully qualified names
            final Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause != null &&
                    JanusGraphException.class.getName().equals(Proxies.unwrap(e).getClass().getName()) &&
                    "com.sleepycat.je.DiskLimitException".equals(rootCause.getClass().getName())) {
                System.err.printf("Execution failed due to disk space issue in the output path. Please check the field 'freeDiskLimit' in the next message: it represents the minimum free space required (in bytes)%n%s%n", ExceptionUtils.getRootCause(e).getMessage());
            } else {
                System.err.println("Execution failed due to: " + e.getMessage());
            }
            e.printStackTrace();
        }

        Util.deleteGraphDataUnlessInhibited(windupConfiguration, graphPath);
    }


    private boolean validateInputAndOutputPath(Collection<Path> inputPaths, Path outputPath) {
        ValidationResult validationResult = OutputPathOption.validateInputsAndOutputPaths(inputPaths, outputPath);
        switch (validationResult.getLevel()) {
            case ERROR:
                System.err.println("ERROR: " + validationResult.getMessage());
                return false;
            case WARNING:
                System.err.println("WARNING: " + validationResult.getMessage());
            default:
                return true;
        }
    }

    private boolean validateOptionValues(Map<String, ConfigurationOption> options, Map<String, Object> optionValues) {
        for (Map.Entry<String, ConfigurationOption> optionEntry : options.entrySet()) {
            ConfigurationOption option = optionEntry.getValue();
            ValidationResult result = option.validate(optionValues.get(option.getName()));

            switch (result.getLevel()) {
                case ERROR:
                    System.err.println("ERROR: " + result.getMessage());
                    return false;
                case PROMPT_TO_CONTINUE:
                    if (!Bootstrap.prompt(result.getMessage(), result.getPromptDefault(), batchMode.get()))
                        return false;
                    break;
                case WARNING:
                    System.err.println("WARNING: " + result.getMessage());
                    break;
                case SUCCESS:
                    break;
            }
        }
        return true;
    }

    private void setDefaultOutputPath(Map<String, Object> optionValues) {
        Object obj = optionValues.getOrDefault(OutputPathOption.NAME, null);
        if (obj == null || (obj instanceof File && StringUtils.isBlank(((File) obj).getPath()))) {
            Iterable<Path> paths = (Iterable<Path>) optionValues.get(InputPathOption.NAME);
            if (paths != null && paths.iterator().hasNext()) {
                try {
                    File canonicalInputFile = paths.iterator().next().toFile().getCanonicalFile();
                    File outputFile = new File(canonicalInputFile.getParentFile(), canonicalInputFile.getName() + ".report");
                    optionValues.put(OutputPathOption.NAME, outputFile);
                } catch (IOException e) {
                    throw new WindupException("Failed to get canonical path for input file: " + paths.iterator().next().toFile());
                }
            }
        }
    }

    private Object convertType(Class<?> type, String input) {
        if (input == null)
            return null;

        if (Path.class.isAssignableFrom(type)) {
            return Paths.get(input);
        } else if (File.class.isAssignableFrom(type)) {
            return new File(input);
        } else if (Boolean.class.isAssignableFrom(type)) {
            return Boolean.valueOf(input);
        } else if (String.class.isAssignableFrom(type)) {
            return input;
        } else {
            throw new RuntimeException("Internal Error! Unrecognized type " + type.getCanonicalName());
        }
    }

    private boolean pathNotEmpty(File f) {
        if (f.exists() && !f.isDirectory()) {
            return true;
        }
        if (f.isDirectory() && f.listFiles() != null && f.listFiles().length > 0) {
            return true;
        }
        return false;
    }

    private WindupProcessor getWindupProcessor() {
        return furnace.getAddonRegistry().getServices(WindupProcessor.class).get();
    }

    private String getOptionName(String argument) {
        if (argument == null)
            return null;
        else if (argument.startsWith("--"))
            return argument.substring(2);
        else if (argument.startsWith("-"))
            return argument.substring(1);
        else
            return null;
    }

    /**
     * Expands the directories from the given list and returns a list of subfiles.
     * Files from the original list are kept as is.
     */
    private static List<Path> expandMultiAppInputDirs(List<Path> input) {
        List<Path> expanded = new LinkedList<>();
        for (Path path : input) {
            if (Files.isRegularFile(path)) {
                expanded.add(path);
                continue;
            }
            if (!Files.isDirectory(path)) {
                String pathString = (path == null) ? "" : path.toString();
                log.warning("Neither a file or directory found in input: " + pathString);
                continue;
            }

            try {
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
                    for (Path subpath : directoryStream) {

                        if (isJavaArchive(subpath)) {
                            expanded.add(subpath);
                        }
                    }
                }
            } catch (IOException e) {
                throw new WindupException("Failed to read directory contents of: " + path);
            }
        }
        return expanded;
    }

    private void setDefaultOptionsValues(Map<String, ConfigurationOption> options, Map<String, Object> optionValues) {
        for (Map.Entry<String, ConfigurationOption> option : options.entrySet()) {
            if (null != optionValues.get(option.getValue().getName()))
                continue;

            optionValues.put(option.getValue().getName(), option.getValue().getDefaultValue());
        }
    }


    /**
     * Removes the .* suffix from the include and exclude packages input.
     */
    private void normalizePackagePrefixes(WindupConfiguration windupConfiguration) {
        List<String> includePackages = windupConfiguration.getOptionValue(ScanPackagesOption.NAME);
        includePackages = normalizePackagePrefixes(includePackages);
        windupConfiguration.setOptionValue(ScanPackagesOption.NAME, includePackages);

        List<String> excludePackages = windupConfiguration.getOptionValue(ExcludePackagesOption.NAME);
        excludePackages = normalizePackagePrefixes(excludePackages);
        windupConfiguration.setOptionValue(ExcludePackagesOption.NAME, excludePackages);
    }


    /**
     * Removes the .* suffix, which is expectable the users will use.
     */
    private static List<String> normalizePackagePrefixes(List<String> packages) {
        if (packages == null)
            return null;

        List<String> result = new ArrayList<>(packages.size());
        for (String pkg : packages) {
            if (pkg.endsWith(".*")) {
                System.out.println("Warning: removing the .* suffix from the package prefix: " + pkg);
            }
            result.add(StringUtils.removeEndIgnoreCase(pkg, ".*"));
        }

        return packages;
    }

    private static boolean isJavaArchive(Path path) {
        return ZipUtil.endsWithZipExtension(path.toString());
    }
}
