package org.jboss.windup.rules.apps.xml.condition.validators;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * This is a part of XmlFile execution. Validator is checking that file name of .xml file matches the queried.
 */
public class XmlFileNameValidator implements XmlFileValidator {
    private RegexParameterizedPatternParser fileNamePattern;

    @Override
    public boolean isValid(GraphRewrite event, EvaluationContext context, XmlFileModel model) {
        if (fileNamePattern != null) {
            final ParameterStore store = DefaultParameterStore.getInstance(context);
            Pattern compiledPattern = fileNamePattern.getCompiledPattern(store);
            String pattern = compiledPattern.pattern();
            String fileName = model.getFileName();
            if (!fileName.matches(pattern)) {
                return false;
            }
            return true;
        }
        return true;
    }

    public void setFileNameRegex(String fileName) {
        this.fileNamePattern = new RegexParameterizedPatternParser(fileName);
    }

    public RegexParameterizedPatternParser getFileNamePattern() {
        return fileNamePattern;
    }

    public Collection<? extends String> getRequiredParamaterNames() {
        if (fileNamePattern != null) {
            return fileNamePattern.getRequiredParameterNames();
        } else {
            return Collections.emptyList();
        }
    }
}
