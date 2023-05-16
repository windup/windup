package org.jboss.windup.rules.apps.javaee.rules;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.JPAConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.JPAPersistenceUnitModel;
import org.jboss.windup.rules.apps.javaee.service.DataSourceService;
import org.jboss.windup.rules.apps.javaee.service.JPAConfigurationFileService;
import org.jboss.windup.rules.apps.javaee.service.JPAPersistenceUnitService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DiscoverJpaConfigurationXmlRuleProviderTest extends AbstractTest {

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testJpaXmlDiscovery() throws IOException {
        try (GraphContext graphContext = factory.create(true)) {
            final String inputPath = "src/test/resources/jpa/xml";
            executeAnalysis(graphContext, inputPath);

            // Validate JPAConfigurationFileModel
            final JPAConfigurationFileService jpaConfigurationFileService = new JPAConfigurationFileService(graphContext);
            final List<JPAConfigurationFileModel> jpaConfigurationFileModels = jpaConfigurationFileService.findAll();
            Assert.assertEquals(6, jpaConfigurationFileModels.size());
            final AtomicBoolean foundJpaXml10 = new AtomicBoolean(false);
            final AtomicBoolean foundJpaXml20 = new AtomicBoolean(false);
            final AtomicBoolean foundJpaXml21 = new AtomicBoolean(false);
            final AtomicBoolean foundJpaXml22 = new AtomicBoolean(false);
            final AtomicBoolean foundJpaXml30 = new AtomicBoolean(false);
            final AtomicBoolean foundJpaXml31 = new AtomicBoolean(false);
            jpaConfigurationFileModels.forEach(jpaConfigurationFileModel -> {
                if ("persistence-1.0.xml".equals(jpaConfigurationFileModel.getFileName()) && "1.0".equals(jpaConfigurationFileModel.getSpecificationVersion())) foundJpaXml10.set(true);
                if ("persistence-2.0.xml".equals(jpaConfigurationFileModel.getFileName()) && "2.0".equals(jpaConfigurationFileModel.getSpecificationVersion())) foundJpaXml20.set(true);
                if ("persistence-2.1.xml".equals(jpaConfigurationFileModel.getFileName()) && "2.1".equals(jpaConfigurationFileModel.getSpecificationVersion())) foundJpaXml21.set(true);
                if ("persistence-2.2.xml".equals(jpaConfigurationFileModel.getFileName()) && "2.2".equals(jpaConfigurationFileModel.getSpecificationVersion())) foundJpaXml22.set(true);
                if ("persistence-3.0.xml".equals(jpaConfigurationFileModel.getFileName()) && "3.0".equals(jpaConfigurationFileModel.getSpecificationVersion())) foundJpaXml30.set(true);
                if ("persistence-3.1.xml".equals(jpaConfigurationFileModel.getFileName()) && "3.1".equals(jpaConfigurationFileModel.getSpecificationVersion())) foundJpaXml31.set(true);
            });
            Assert.assertTrue(foundJpaXml10.get());
            Assert.assertTrue(foundJpaXml20.get());
            Assert.assertTrue(foundJpaXml21.get());
            Assert.assertTrue(foundJpaXml22.get());
            Assert.assertTrue(foundJpaXml30.get());
            Assert.assertTrue(foundJpaXml31.get());

            // Validate JPAPersistenceUnitModel
            final JPAPersistenceUnitService jpaPersistenceUnitService = new JPAPersistenceUnitService(graphContext);
            final List<JPAPersistenceUnitModel> jpaPersistenceUnitModels = jpaPersistenceUnitService.findAll();
            Assert.assertEquals(6, jpaPersistenceUnitModels.size());
            jpaPersistenceUnitModels.forEach(jpaPersistenceUnitModel -> Assert.assertEquals(5, jpaPersistenceUnitModel.getProperties().size()));

            // Validate
            final DataSourceService dataSourceService = new DataSourceService(graphContext);
            List<DataSourceModel> dataSourceModels = dataSourceService.findAll();
            final AtomicBoolean foundPostgreSQL = new AtomicBoolean(false);
            final AtomicBoolean foundDB2 = new AtomicBoolean(false);
            final AtomicBoolean foundInformix = new AtomicBoolean(false);
            final AtomicBoolean foundOracle = new AtomicBoolean(false);
            final AtomicBoolean foundMySQL = new AtomicBoolean(false);
            final AtomicBoolean foundSybase = new AtomicBoolean(false);
            dataSourceModels.forEach(dataSourceModel -> {
                System.out.println(dataSourceModel);
                if ("Sybase".equals(dataSourceModel.getDatabaseTypeName())) foundSybase.set(true);
                if ("DB2".equals(dataSourceModel.getDatabaseTypeName())) foundDB2.set(true);
                if ("org.hibernate.dialect.InformixDialect".equals(dataSourceModel.getDatabaseTypeName())) foundInformix.set(true);
                if ("Oracle".equals(dataSourceModel.getDatabaseTypeName())) foundOracle.set(true);
                if ("MySQL".equals(dataSourceModel.getDatabaseTypeName())) foundMySQL.set(true);
                if ("PostgreSQL".equals(dataSourceModel.getDatabaseTypeName())) foundPostgreSQL.set(true);
            });
            Assert.assertTrue(foundPostgreSQL.get());
            Assert.assertTrue(foundDB2.get());
            Assert.assertTrue(foundInformix.get());
            Assert.assertTrue(foundOracle.get());
            Assert.assertTrue(foundMySQL.get());
            Assert.assertTrue(foundSybase.get());

            // Validate all the technology tags have been found with versions
            final TechnologyTagService technologyTagService = new TechnologyTagService(graphContext);
            final List<TechnologyTagModel> technologyTagModels = technologyTagService.findAll();
            Assert.assertEquals(6, technologyTagModels.size());
            final AtomicBoolean foundJpaXml10TechTag = new AtomicBoolean(false);
            final AtomicBoolean foundJpaXml20TechTag = new AtomicBoolean(false);
            final AtomicBoolean foundJpaXml21TechTag = new AtomicBoolean(false);
            final AtomicBoolean foundJpaXml22TechTag = new AtomicBoolean(false);
            final AtomicBoolean foundJpaXml30TechTag = new AtomicBoolean(false);
            final AtomicBoolean foundJpaXml31TechTag = new AtomicBoolean(false);
            technologyTagModels.forEach(technologyTagModel -> {
                if ("JPA XML".equals(technologyTagModel.getName()) && "1.0".equals(technologyTagModel.getVersion())) foundJpaXml10TechTag.set(true);
                if ("JPA XML".equals(technologyTagModel.getName()) && "2.0".equals(technologyTagModel.getVersion())) foundJpaXml20TechTag.set(true);
                if ("JPA XML".equals(technologyTagModel.getName()) && "2.1".equals(technologyTagModel.getVersion())) foundJpaXml21TechTag.set(true);
                if ("JPA XML".equals(technologyTagModel.getName()) && "2.2".equals(technologyTagModel.getVersion())) foundJpaXml22TechTag.set(true);
                if ("JPA XML".equals(technologyTagModel.getName()) && "3.0".equals(technologyTagModel.getVersion())) foundJpaXml30TechTag.set(true);
                if ("JPA XML".equals(technologyTagModel.getName()) && "3.1".equals(technologyTagModel.getVersion())) foundJpaXml31TechTag.set(true);
            });
            Assert.assertTrue(foundJpaXml10TechTag.get());
            Assert.assertTrue(foundJpaXml20TechTag.get());
            Assert.assertTrue(foundJpaXml21TechTag.get());
            Assert.assertTrue(foundJpaXml22TechTag.get());
            Assert.assertTrue(foundJpaXml30TechTag.get());
            Assert.assertTrue(foundJpaXml31TechTag.get());
        }
    }
}
