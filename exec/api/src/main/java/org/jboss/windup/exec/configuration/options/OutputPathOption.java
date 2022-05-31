package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractPathConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

/**
 * Indicates that output path for the windup report and other data produced by a Windup execution.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class OutputPathOption extends AbstractPathConfigurationOption {
    public static final String NAME = "output";

    public OutputPathOption() {
        super(false);
    }

    /**
     * <p>
     * This validates that the input and output options are compatible.
     * </p>
     * <p>
     * Examples of incompatibilities would include:
     * </p>
     * <ul>
     * <li>Input and Output path are the same</li>
     * <li>Input is a subdirectory of the output</li>
     * <li>Output is a subdirectory of the input</li>
     * </ul>
     */
    public static ValidationResult validateInputAndOutputPath(Path inputPath, Path outputPath) {
        return validateInputsAndOutputPaths(Collections.singletonList(inputPath), outputPath);
    }

    @SuppressWarnings("rawtypes")
    public static ValidationResult validateInputsAndOutputPaths(Collection inputPaths, Path outputPath) {

        if (inputPaths == null) {
            return new ValidationResult(ValidationResult.Level.ERROR, "Input path must be specified.");
        }

        if (inputPaths.isEmpty()) {
            return new ValidationResult(ValidationResult.Level.ERROR, "Couldn't find any application at the root level of the directory. Use `--sourceMode` if the directory contains source files you want to analyse.");
        }

        if (outputPath == null) {
            return new ValidationResult(ValidationResult.Level.ERROR, "Output path must be specified.");
        }

        File outputFile = outputPath.toFile();

        boolean nonNullInputFound = false;
        for (Object inputPath : inputPaths) {
            File inputFile = (inputPath instanceof Path) ? ((Path) inputPath).toFile() : (File) inputPath;
            if (inputFile == null) {
                continue;
            }

            if (inputFile.equals(outputFile)) {
                return new ValidationResult(ValidationResult.Level.ERROR, "Output file cannot be the same as the input file.");
            }

            File inputParent = inputFile.getParentFile();
            while (inputParent != null) {
                if (inputParent.equals(outputFile)) {
                    return new ValidationResult(ValidationResult.Level.ERROR, "Output path must not be a parent of input path.");
                }
                inputParent = inputParent.getParentFile();
            }

            File outputParent = outputFile.getParentFile();
            while (outputParent != null) {
                if (outputParent.equals(inputFile)) {
                    return new ValidationResult(ValidationResult.Level.ERROR, "Input path must not be a parent of output path.");
                }
                outputParent = outputParent.getParentFile();
            }
            nonNullInputFound = true;
        }

        if (!nonNullInputFound) {
            return new ValidationResult(ValidationResult.Level.ERROR, "Input path must be specified.");
        }


        return ValidationResult.SUCCESS;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Output";
    }

    @Override
    public String getDescription() {
        return "Output Directory (WARNING: any existing files will be removed).";
    }

    @Override
    public InputType getUIType() {
        return InputType.DIRECTORY;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public int getPriority() {
        return 9000;
    }
}
