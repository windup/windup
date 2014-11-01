package org.jboss.windup.gui;

import javax.inject.Inject;

import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.windup.gui.components.MainWindupFrame;

public class WindupGuiCommand implements UICommand
{
    @Inject
    private MainWindupFrame windupFrame;

    @Override
    public UICommandMetadata getMetadata(UIContext ctx)
    {
        return Metadata.forCommand(getClass()).name("Windup Java Gui")
                    .description("Run the Windup Graphical User Interface for Java Application Migration")
                    .category(Categories.create("Platform", "Migration"));
    }

    @Override
    public void validate(UIValidationContext context)
    {

    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception
    {
    }

    @Override
    public boolean isEnabled(UIContext context)
    {
        return true;
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception
    {
        windupFrame.setVisible(true);
        while (windupFrame.isVisible())
        {
            try
            {
                Thread.sleep(500L);
            }
            catch (InterruptedException e)
            {
                // ignore
            }
        }
        return Results.success();
    }
}
