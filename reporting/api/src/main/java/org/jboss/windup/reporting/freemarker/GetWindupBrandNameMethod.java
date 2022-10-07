package org.jboss.windup.reporting.freemarker;

import freemarker.template.TemplateModelException;
import org.jboss.windup.util.ThemeProvider;

import java.util.List;

/**
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class GetWindupBrandNameMethod implements WindupFreeMarkerMethod {
    @Override
    public String getDescription() {
        return "Returns the tool name for the current theme.";
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        return ThemeProvider.getInstance().getTheme().getBrandName();
    }
}
