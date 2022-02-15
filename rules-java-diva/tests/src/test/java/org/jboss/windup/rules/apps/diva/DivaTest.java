package org.jboss.windup.rules.apps.diva;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.rules.apps.diva.model.DivaAppModel;
import org.jboss.windup.rules.apps.diva.model.DivaOpModel;
import org.jboss.windup.rules.apps.diva.model.DivaTxModel;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.util.Assert;

@RunWith(Arquillian.class)
public class DivaTest {

    @Deployment
    @AddonDependencies({ @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-diva"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi") })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testSpring() throws IOException {
        try (GraphContext context = factory.create(true)) {
            Path inputPath = Paths.get("src/test/resources/spring/");
            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "Windup")
                    .resolve(UUID.randomUUID().toString());

            WindupConfiguration windupConfiguration = new WindupConfiguration().setGraphContext(context)
                    .setOptionValue(SourceModeOption.NAME, true)
                    .setOptionValue(EnableTransactionAnalysisOption.NAME, true).addInputPath(inputPath)
                    .setOutputDirectory(outputPath);

            processor.execute(windupConfiguration);

            List<? extends ProjectModel> projects = IterableUtils.toList(context.findAll(DivaAppModel.class));
            Assert.equals(projects.size(), 1);
            List<? extends DivaTxModel> txs = IterableUtils.toList(context.findAll(DivaTxModel.class));
            Assert.equals(txs.size(), 1);
            List<?> ops = context.getQuery(DivaOpModel.class).getRawTraversal()
                    .has("sql", P.without("BEGIN", "COMMIT", "ROLLBACK")).toList();
            Assert.equals(ops.size(), 2);

        }
    }

    @Test
    public void testServlet() throws IOException {
        try (GraphContext context = factory.create(true)) {
            Path inputPath = Paths.get("src/test/resources/servlet/");
            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "Windup")
                    .resolve(UUID.randomUUID().toString());

            WindupConfiguration windupConfiguration = new WindupConfiguration().setGraphContext(context)
                    .setOptionValue(SourceModeOption.NAME, true)
                    .setOptionValue(EnableTransactionAnalysisOption.NAME, true).addInputPath(inputPath)
                    .setOutputDirectory(outputPath);

            processor.execute(windupConfiguration);

            List<? extends ProjectModel> projects = IterableUtils.toList(context.findAll(DivaAppModel.class));
            Assert.equals(projects.size(), 1);
            List<? extends DivaTxModel> txs = IterableUtils.toList(context.findAll(DivaTxModel.class));
            Assert.equals(txs.size(), 1);
            List<?> ops = context.getQuery(DivaOpModel.class).getRawTraversal()
                    .has("sql", P.without("BEGIN", "COMMIT", "ROLLBACK")).toList();
            Assert.equals(ops.size(), 1);
        }
    }

    @Ignore
    @Test
    public void testDayTrader() throws IOException {
        try (GraphContext context = factory.create(true)) {
            Path inputPath = Paths.get("../../../sample.daytrader7");
            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "Windup")
                    .resolve(UUID.randomUUID().toString());

            WindupConfiguration windupConfiguration = new WindupConfiguration().setGraphContext(context)
                    .setOptionValue(SourceModeOption.NAME, true).setOptionValue(EnableTransactionAnalysisOption.NAME, true).addInputPath(inputPath).setOutputDirectory(outputPath);

            processor.execute(windupConfiguration);
        }
    }

}
