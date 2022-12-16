package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.util.ThemeProvider;

import freemarker.template.TemplateModelException;

public class GetWindupTopBarTitleMethod implements WindupFreeMarkerMethod {
    @Override
    public String getDescription() {
        return "Returns the tool top bar title for the current theme.";
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        return ThemeProvider.getInstance().getTheme().getTopBarTitle();
    }
}
