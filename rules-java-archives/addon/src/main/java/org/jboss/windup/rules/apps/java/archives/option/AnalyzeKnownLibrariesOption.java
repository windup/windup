package org.jboss.windup.rules.apps.java.archives.option;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Option to force the analysis of already known libraries. This will apply to both maven-central dependencies present in
 * our maven-central lucene index and general 3rd party libraries found within the input applications.
 * 
 * @author <a href="mailto:jleflete@gmail.com">Juan Manuel Leflet Estrada</a>
 */
public class AnalyzeKnownLibrariesOption extends AbstractConfigurationOption {
    
    public static final String NAME = "analyzeKnownLibraries";
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Force analysis of known libraries";
    }

    @Override
    public String getDescription() {
        return "Force the analysis of libraries present in the input application(s) even if they are present in the Lucene index or they are part of the input applications' organization.";
    }

    @Override
    public Class<?> getType() {
        return Boolean.class;
    }

    @Override
    public InputType getUIType() {
        return InputType.SINGLE;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public Object getDefaultValue() {
        return false;
    }

    @Override
    public ValidationResult validate(Object value) {
        return ValidationResult.SUCCESS;
    }
}
