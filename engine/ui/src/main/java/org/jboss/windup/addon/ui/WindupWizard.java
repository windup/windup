package org.jboss.windup.addon.ui;

import java.util.logging.Logger;

import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizard;

public class WindupWizard implements UIWizard, UICommand
{
    private static Logger log = Logger.getLogger(WindupWizard.class.getName());

    @Override
    public UICommandMetadata getMetadata(UIContext ctx)
    {
        return Metadata.forCommand(getClass()).name("Windup Migrate App").description("Run Windup Migration Analyzer")
                    .category(Categories.create("Platform", "Migration"));
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void validate(UIValidationContext context)
    {

    }

    @Override
    public boolean isEnabled(UIContext context)
    {
        return true;
    }

    @Override
    public NavigationResult next(UINavigationContext context) throws Exception
    {
        return null;
    }
}
