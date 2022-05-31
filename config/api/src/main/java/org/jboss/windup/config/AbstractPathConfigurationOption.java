package org.jboss.windup.config;

import org.jboss.windup.util.exception.WindupException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Provides a base for validating {@link ConfigurationOption}s of type {@link File}. This uses the results of {@link ConfigurationOption#getUIType()}
 * to determine whether to validate as a file or as a directory.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public abstract class AbstractPathConfigurationOption extends AbstractConfigurationOption {
    private boolean mustExist = false;

    /**
     * If mustExist is set to true, then the path will fail to validate if it does not already exist.
     */
    protected AbstractPathConfigurationOption(boolean mustExist) {
        this.mustExist = mustExist;
    }

    @Override
    public Class<?> getType() {
        return File.class;
    }

    protected Path castToPath(Object file) {
        if (file instanceof File)
            return ((File) file).toPath();
        else if (file instanceof Path)
            return (Path) file;
        else
            throw new WindupException("Unrecognized type: " + file.getClass().getCanonicalName());
    }

    private ValidationResult validatePath(Path path) {
        if (mustExist) {
            if (getUIType() == InputType.DIRECTORY && !Files.isDirectory(path)) {
                return new ValidationResult(ValidationResult.Level.ERROR, getName() + " must exist and be a directory!");
            } else if (getUIType() == InputType.FILE && !Files.isRegularFile(path)) {
                return new ValidationResult(ValidationResult.Level.ERROR, getName() + " must exist and be a regular file!");
            } else if (getUIType() == InputType.FILE_OR_DIRECTORY && !Files.exists(path)) {
                return new ValidationResult(ValidationResult.Level.ERROR, getName() + " must exist!");
            } else if (!Files.exists(path)) {
                return new ValidationResult(ValidationResult.Level.ERROR, getName() + " must exist!");
            }
        }
        return ValidationResult.SUCCESS;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ValidationResult validate(Object fileObject) {
        if ((fileObject == null && isRequired()) ||
                (fileObject instanceof Collection && isRequired() && ((Collection) fileObject).isEmpty())) {
            return new ValidationResult(ValidationResult.Level.ERROR, getName() + " must be specified.");
        } else if (fileObject == null) {
            return ValidationResult.SUCCESS;
        }

        // Path isn't the type of iterable we are looking for
        if (fileObject instanceof Iterable && !(fileObject instanceof Path)) {
            for (Object listItem : (Iterable) fileObject) {
                ValidationResult result = validatePath(castToPath(listItem));
                if (result.getLevel() != ValidationResult.Level.SUCCESS)
                    return result;
            }
            return ValidationResult.SUCCESS;
        }

        return validatePath(castToPath(fileObject));
    }
}
