package org.jboss.windup.exec.configuration.options;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import org.jboss.windup.config.AbstractPathConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Specifies the Input path for Windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author Ondrej Zizka
 */
public class InputPathOption extends AbstractPathConfigurationOption
{
    public static final String NAME = "input";
    private static final long SIZE_WARNING_TRESHOLD_MB = 10;

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
        return "Input paths";
    }

    @Override
    public String getDescription()
    {
        return "Input file or directory (a directory is required for source mode)";
    }

    @Override
    public ValidationResult validate(Object filesObject)
    {
        ValidationResult result = super.validate(filesObject);

        ///if (!(filesObject instanceof File) && !(filesObject instanceof Path))
        ///    return result;

        if (!result.isSuccess())
            return result;

        // This method is called again from super.validate() for each item, so let's skip that.
        if (!(filesObject instanceof Iterable))
            return ValidationResult.SUCCESS;

        //File file = super.castToFile(filesObject);

        List<String> largeApps = new LinkedList<>();
        Iterable it = (Iterable) filesObject;
        for (Object fileObj : it)
        {
            File file = super.castToFile(fileObj);
            ValidationResult resultFile = super.validate(file);
            if (!resultFile.isSuccess())
                return resultFile;

            if (!file.exists())
                return new ValidationResult(ValidationResult.Level.ERROR, "Input path not found: " + file.getAbsolutePath());

            if (FileUtils.sizeOf(file) > SIZE_WARNING_TRESHOLD_MB * 1024 * 1024){
                largeApps.add(file.getPath());
            }
        }

        if (!largeApps.isEmpty())
            return new ValidationResult(ValidationResult.Level.PROMPT_TO_CONTINUE,
                "These input applications or directories are large:"
                + "\n\t" + StringUtils.join(largeApps, "\n\t") + "\n"
                + " Processing may take a very long time."
                + " Please consult the Windup User Guide for performance tips."
                + " Would you like to continue?", true);

        return ValidationResult.SUCCESS;
    }

    @Override
    public InputType getUIType()
    {
        return InputType.MANY;
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
