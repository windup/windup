package org.jboss.windup.rules.apps.javaee;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class AbstractTest {

    @Inject
    private WindupProcessor processor;

    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addClass(AbstractTest.class).addBeansXML();
    }

    public void executeAnalysis(GraphContext context, String inputPathString) throws IOException {
        ProjectModel pm = context.getFramed().addFramedVertex(ProjectModel.class);
        pm.setName("Main Project");
        FileModel inputPath = context.getFramed().addFramedVertex(FileModel.class);
        inputPath.setFilePath(inputPathString);

        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "Windup").resolve(UUID.randomUUID().toString());
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        pm.addFileModel(inputPath);
        pm.setRootFileModel(inputPath);
        WindupConfiguration windupConfiguration = new WindupConfiguration()
                .setGraphContext(context);
        windupConfiguration.setOptionValue(SourceModeOption.NAME, true);
        windupConfiguration.addInputPath(Paths.get(inputPath.getFilePath()));
        windupConfiguration.setOutputDirectory(outputPath);
        processor.execute(windupConfiguration);
    }
}
