package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractPathConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.config.loader.LabelLoader;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Indicates the file that will contain custom labels provided by the user. Multiple paths can be specified separated by a space (for example,
 * --userLabelsDirectory PATH_1 PATH_2).
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 *
 */
public class UserLabelsDirectoryOption extends AbstractPathConfigurationOption
{

    public static final String NAME = "userLabelsDirectory";

    @Inject
    private LabelLoader labelLoader;

    public UserLabelsDirectoryOption()
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
        return "User Metadata Directory";
    }

    @Override
    public String getDescription()
    {
        return "User Labels Directory (Search pattern: *.windup.label.xml, *.rhamt.label.xml). Multiple paths can be specified separated by a space (for example, --userLabelsDirectory PATH_1 PATH_2).";
    }

    @Override
    public Class<?> getType()
    {
        return File.class;
    }

    @Override
    public InputType getUIType()
    {
        return InputType.MANY;
    }

    @Override
    public boolean isRequired()
    {
        return false;
    }

    @Override
    public int getPriority()
    {
        return 7500;
    }

    @Override
    public ValidationResult validate(Object fileObject)
    {
        ValidationResult validate = super.validate(fileObject);
        if (validate.getLevel().equals(ValidationResult.Level.ERROR))
        {
            return validate;
        }

        /*
         * In order to validate labels before running an analysis, this block of code will force to load all labels so LabelLoader can validate them
         * and detect if there are more than one 'LabelSet' using the same ID.
         */
        List<Path> userLabelsPaths = new ArrayList<>();
        if (fileObject != null)
        {
            if (fileObject instanceof Iterable && !(fileObject instanceof Path))
            {
                for (Object listItem : (Iterable) fileObject)
                {
                    userLabelsPaths.add(castToPath(listItem));
                }
            }
            else
            {
                userLabelsPaths.add(castToPath(fileObject));
            }
        }

        List<Path> defaultRulePaths = new ArrayList<>();

        defaultRulePaths.add(PathUtil.getWindupRulesDir());
        defaultRulePaths.add(PathUtil.getUserRulesDir());
        defaultRulePaths.add(PathUtil.getWindupLabelsDir());
        defaultRulePaths.add(PathUtil.getUserLabelsDir());
        defaultRulePaths.addAll(userLabelsPaths);

        try
        {
            RuleLoaderContext labelLoaderContext = new RuleLoaderContext(defaultRulePaths, null);
            labelLoader.loadConfiguration(labelLoaderContext);
        }
        catch (WindupException e)
        {
            return new ValidationResult(ValidationResult.Level.ERROR, e.getMessage());
        }

        return validate;
    }
}
