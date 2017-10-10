package org.jboss.windup.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.windup.util.exception.WindupException;

/**
 * Provides a base for validating {@link ConfigurationOption}s of type {@link File}. This uses the results of {@link ConfigurationOption#getUIType()}
 * to determine whether to validate as a file or as a directory.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public abstract class AbstractPathConfigurationOption extends AbstractConfigurationOption
{
    private boolean mustExist = false;

    /**
     * If mustExist is set to true, then the path will fail to validate if it does not already exist.
     */
    protected AbstractPathConfigurationOption(boolean mustExist)
    {
        this.mustExist = mustExist;
    }

    @Override
    public Class<?> getType()
    {
        return File.class;
    }

    protected Path castToPath(Object file)
    {
        if (file instanceof File)
            return ((File) file).toPath();
        else if (file instanceof Path)
            return (Path) file;
        else
            throw new WindupException("Unrecognized type: " + file.getClass().getCanonicalName());
    }

    private ValidationResult validatePath(Path path)
    {
        if (mustExist)
        {
            if (getUIType() == InputType.DIRECTORY && !Files.isDirectory(path))
            {
                return new ValidationResult(ValidationResult.Level.ERROR, "Option " + getName() + " is not an existing directory: " + path);
            }
            else if (getUIType() == InputType.FILE && !Files.isRegularFile(path))
            {
                return new ValidationResult(ValidationResult.Level.ERROR, "Option " + getName() + " is not an existing regular file: " + path);
            }
            else if (getUIType() == InputType.FILE_OR_DIRECTORY && !Files.exists(path))
            {
                return new ValidationResult(ValidationResult.Level.ERROR, "Option " + getName() + " is not an existing file or directory: " + path);
            }
            else if (!Files.exists(path))
            {
                return new ValidationResult(ValidationResult.Level.ERROR, "Option " + getName() + " is not an existing path: " + path);
            }
        }
        return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult validate(Object fileObject)
    {
        if (fileObject == null && isRequired())
        {
            return new ValidationResult(ValidationResult.Level.ERROR, getName() + " is required!");
        }
        else if (fileObject == null)
        {
            return ValidationResult.SUCCESS;
        }

        // Path isn't the type of iterable we are looking for
        if (fileObject instanceof Iterable && !(fileObject instanceof Path))
        {
            for (Object listItem : (Iterable) fileObject)
            {
                ValidationResult result = validatePath(castToPath(listItem));
                if (result.getLevel() != ValidationResult.Level.SUCCESS)
                    return result;
            }
            return ValidationResult.SUCCESS;
        }

        return validatePath(castToPath(fileObject));
    }
}
