package org.jboss.windup.ext.groovy;

import javax.enterprise.event.Observes;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.event.PostStartup;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.javascanner.ast.event.JavaScannerASTEvent;
import org.jboss.windup.util.exception.WindupException;

public class GroovyDSLSupport
{
    private static Furnace furnace;

    public void onJavaScannerASTEvent(@Observes JavaScannerASTEvent event)
    {
        System.out.println("Received JavaScannerASTEvent: " + event);
    }

    public void setFurnace(@Observes PostStartup event, Furnace furnace)
    {
        GroovyDSLSupport.furnace = furnace;
    }

    private static GraphContext getGraphContext()
    {
        Imported<GraphContext> contexts = furnace.getAddonRegistry().getServices(GraphContext.class);
        return contexts.get();
    }

    public static void registerInterest(String className, String importance, String hint)
    {
        GraphContext graphContext = GroovyDSLSupport.getGraphContext();

        if (graphContext == null)
        {
            throw new WindupException("Failed miserably");
        }
    }
}
