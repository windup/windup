package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.util.ThemeProvider;

import freemarker.template.TemplateModelException;

public class GetWindupBrandWebsiteUrlMethod implements WindupFreeMarkerMethod {
    @Override
    public String getDescription() {
        return "Returns the tool website URL for the current theme.";
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        return ThemeProvider.getInstance().getTheme().getBrandWebsiteUrl();
    }
}
