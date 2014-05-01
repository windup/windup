package org.jboss.windup.addon.reporting;

import java.io.File;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.runner.DefaultEvaluationContext;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextImpl;
import org.junit.Test;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

public class ApplicationReportTest extends AbstractTestCase
{

    @Test
    public void testApplicationReport() throws Exception
    {
        Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    /*
                     * If all conditions of the .when() clause were satisfied, the following conditions will be
                     * evaluated
                     */
                    .perform(
                            ApplicationReport.create()
                                .applicationName("Tactical Analysis")
                                .applicationVersion("12.3.4.12")
                                .applicationCreator("OCP")
                    );
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        evaluationContext.put(ParameterValueStore.class, values);
        
        final File folder = File.createTempFile("windupGraph", "");
        final GraphContext context = new GraphContextImpl(folder);
        Subset.evaluate(configuration).perform(new GraphRewrite(context), evaluationContext);
    }
}
