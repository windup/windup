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
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.util.Assert;

import io.tackle.diva.windup.DivaRuleProvider;
import io.tackle.diva.windup.model.DivaAppModel;
import io.tackle.diva.windup.model.DivaOpModel;
import io.tackle.diva.windup.model.DivaTxModel;

@RunWith(Arquillian.class)
public class DivaTest {

    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addClass(DivaTest.class).addBeansXML();
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Inject
    private DivaRuleProvider divaRuleProvider;

    @Test
    public void testSpring() throws IOException {
        try (GraphContext context = factory.create(true)) {
            Path inputPath = Paths.get("src/test/resources/spring/");
            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "Windup")
                    .resolve(UUID.randomUUID().toString());

            WindupConfiguration windupConfiguration = new WindupConfiguration().setGraphContext(context)
                    .setOptionValue(SourceModeOption.NAME, true).addInputPath(inputPath).setOutputDirectory(outputPath);

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
                    .setOptionValue(SourceModeOption.NAME, true).addInputPath(inputPath).setOutputDirectory(outputPath);

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

}
