package org.jboss.windup.exec.configuration.options;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.AbstractPathConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.util.Util;

/**
 * Specifies the Input path for Windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author Ondrej Zizka
 */
public class InputPathOption extends AbstractPathConfigurationOption
{
    private static Logger LOG = Logger.getLogger(InputPathOption.class.getCanonicalName());

    public static final String NAME = "input";
    private static final long SIZE_WARNING_TRESHOLD_MB = 10;

    public InputPathOption()
    {
        super(true);
    }

    @Override
    public Class<?> getType()
    {
        return Path.class;
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
        return "Input file or directory (a directory is required for source mode). Multiple paths can be specified separated by a space (for example, --input PATH_1 PATH_2).";
    }

    @Override
    public ValidationResult validate(Object filesObject)
    {
        ValidationResult result = super.validate(filesObject);
        if (!result.isSuccess())
            return result;

        List<Path> largeApps = new LinkedList<>();
        for (Path path : (Iterable<Path>) filesObject)
        {
            ValidationResult resultFile = super.validate(path);
            if (!resultFile.isSuccess())
                return resultFile;

            if (!Files.exists(path))
                return new ValidationResult(ValidationResult.Level.ERROR, "Input path not found: " + path);

            if (!Files.isDirectory(path))
            {
                try
                {
                    long fileSize = Files.size(path);
                    if (fileSize > SIZE_WARNING_TRESHOLD_MB * 1024 * 1024)
                    {
                        largeApps.add(path);
                    }
                }
                catch (IOException e)
                {
                    LOG.warning("Could not determine file size for: " + path);
                }
            }
        }

        if (!largeApps.isEmpty())
            return new ValidationResult(ValidationResult.Level.PROMPT_TO_CONTINUE,
                        "These input applications or directories are large:"
                                    + "\n\t" + StringUtils.join(largeApps, "\n\t") + "\n"
                                    + " Processing may take a very long time."
                                    + " Please consult the "+Util.WINDUP_BRAND_NAME_ACRONYM+" User Guide for performance tips."
                                    + " Would you like to continue?",
                        true);

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
