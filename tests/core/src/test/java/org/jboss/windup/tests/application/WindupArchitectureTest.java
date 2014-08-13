package org.jboss.windup.tests.application;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.junit.Assert;

public abstract class WindupArchitectureTest
{

    void runTest(WindupProcessor processor, GraphContext graphContext, String inputPath, boolean sourceMode)
                throws Exception
    {
        Assert.assertNotNull(processor);
        Assert.assertNotNull(processor.toString());

        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "WindupReport");
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        processor.setOutputDirectory(outputPath);

        WindupConfigurationModel windupCfg = graphContext.getFramed().addVertex(null, WindupConfigurationModel.class);
        windupCfg.setInputPath(inputPath);
        windupCfg.setSourceMode(sourceMode);

        windupCfg.setOutputPath(outputPath.toAbsolutePath().toString());
        windupCfg.setSourceMode(false);

        processor.execute();
    }
}
