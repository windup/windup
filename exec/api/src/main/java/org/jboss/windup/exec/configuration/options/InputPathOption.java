package org.jboss.windup.exec.configuration.options;

import java.io.File;
import org.jboss.windup.config.AbstractPathConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Specifies the Input path for Windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class InputPathOption extends AbstractPathConfigurationOption
{
    public static final String NAME = "input";
    private static long SIZE_WARNING_TRESHOLD_MB = 10;

    public InputPathOption()
    {
        super(true);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Input path";
    }

    @Override
    public String getDescription()
    {
        return "Input file or directory (a directory is required for source mode)";
    }

    @Override
    public ValidationResult validate(Object fileObject)
    {
        ValidationResult result = super.validate(fileObject);
        if (!result.isSuccess())
            return result;

        File file = ((File) fileObject);
        if (!file.exists())
            return new ValidationResult(ValidationResult.Level.ERROR, "Input path not found: " + file.getAbsolutePath());

        if (file.isFile())
        {
            if (file.length() > SIZE_WARNING_TRESHOLD_MB * 1024 * 1024)
            {
                return new ValidationResult(ValidationResult.Level.PROMPT_TO_CONTINUE,
                            "The input application is large. Processing may take a very long time. "
                                        + "Please consult the Windup User Guide for performance tips.");
            }
        }

        return ValidationResult.SUCCESS;
    }

    @Override
    public InputType getUIType()
    {
        return InputType.FILE_OR_DIRECTORY;
    }

    @Override
    public boolean isRequired()
    {
        return true;
    }

    @Override
    public int getPriority()
    {
        return 10000;
    }
}
