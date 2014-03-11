package org.jboss.windup.configuration;

import java.io.File;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.junit.Test;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationLoader;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

public class ReadXMLConfigurationTest
{
   @Test
   public void testLoadingFile() throws Exception
   {
      File folder = File.createTempFile("windupGraph", "");
      GraphContext context = new GraphContext(folder);
      ConfigurationLoader loader = ConfigurationLoader.create(context);
      Configuration configuration = loader.loadConfiguration(context);

      EvaluationContextImpl evaluationContext = new EvaluationContextImpl();

      DefaultParameterValueStore values = new DefaultParameterValueStore();
      evaluationContext.put(ParameterValueStore.class, values);

      Subset.evaluate(configuration).perform(new GraphRewrite(), evaluationContext);
   }
}
