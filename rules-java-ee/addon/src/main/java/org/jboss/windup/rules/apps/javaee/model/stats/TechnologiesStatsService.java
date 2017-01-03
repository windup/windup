package org.jboss.windup.rules.apps.javaee.model.stats;

import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.BelongsToProject;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;

import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.pipes.filter.BackFilterPipe;
import com.tinkerpop.pipes.transform.OutPipe;
import com.tinkerpop.pipes.util.Pipeline;
import com.tinkerpop.pipes.util.StartPipe;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.CollectionUtils;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.model.JarArchiveModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.EjbBeanBaseModel;
import org.jboss.windup.rules.apps.javaee.model.EjbEntityBeanModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.model.JPAEntityModel;
import org.jboss.windup.rules.apps.javaee.model.JPANamedQueryModel;
import org.jboss.windup.rules.apps.javaee.model.JPAPersistenceUnitModel;
import org.jboss.windup.rules.apps.javaee.model.JaxRSWebServiceModel;
import org.jboss.windup.rules.apps.javaee.model.JaxWSWebServiceModel;
import org.jboss.windup.rules.apps.javaee.model.JmsConnectionFactoryModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationType;
import org.jboss.windup.rules.apps.javaee.model.RMIServiceModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateMappingFileModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateSessionFactoryModel;
import org.jboss.windup.util.Logging;

/**
 * Functionality for the Technologies Report.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public class TechnologiesStatsService extends GraphService<TechnologiesStatsModel>
{
    private final static Logger LOG = Logging.get(TechnologiesStatsService.class);

    private Map<ProjectModel, ProjectModel> projectModelToRootProjectModel;

    public TechnologiesStatsService(GraphContext context)
    {
        super(context, TechnologiesStatsModel.class);

        ProjectService projectService = new ProjectService(context);
        this.projectModelToRootProjectModel = projectService.getProjectToRootProjectMap();
    }

    protected void setCountFilesByType(TechnologiesStatsModel stats, Map<String, Integer> suffixToCount)
    {
        // This will need to filter out archives.
        stats.setStatsFilesByTypeJava(item(
                suffixToCount.getOrDefault("class", 0)
                 + suffixToCount.getOrDefault("java", 0)
        ));
        stats.setStatsFilesByTypeJs(item(suffixToCount.getOrDefault("js", 0)));
        stats.setStatsFilesByTypeHtml(item(suffixToCount.getOrDefault("html", 0)));
        stats.setStatsFilesByTypeCss(item(suffixToCount.getOrDefault("css", 0)));
        stats.setStatsFilesByTypeXml(item(suffixToCount.getOrDefault("xml", 0)));
        stats.setStatsFilesByTypeFmt(item(suffixToCount.getOrDefault("fmt", 0)));
    }

    protected void setTechnologiesUsage(TechnologiesStatsModel stats, Map<String, Integer> technologyUsage)
    {
        stats.setStatsServicesEjbStateless(item(stats.getStat(TechnologiesStatsModel.STATS_SERVICES_EJB_STATELESS)));
        stats.setStatsServicesEjbStateful(item(stats.getStat(TechnologiesStatsModel.STATS_SERVICES_EJB_STATEFUL)));
        stats.setStatsServicesEjbMessageDriven(item(stats.getStat(TechnologiesStatsModel.STATS_SERVICES_EJB_MESSAGEDRIVEN)));

        // TODO: Finish this.
        // TODO: This would deserve refactoring to key-value pair

        technologyUsage.entrySet().forEach(entry -> stats.setStat(entry.getKey(), entry.getValue()));
    }

    /**
     * Compute the stats for this execution.
     */
    public TechnologiesStatsModel computeStats(Map<String, Integer> suffixToCount, Map<String, Integer> technologyUsage)
    {
        TechnologiesStatsModel stats = this.create();
        stats.setComputed(new Date());

        this.setCountFilesByType(stats, suffixToCount);
        this.setTechnologiesUsage(stats, technologyUsage);

        this.commit();

        return stats;
    }

    public Map<ProjectModel, Map<String, Integer>> countTechnologiesUsage()
    {
        Map<String, Map<ProjectModel, Integer>> technologyUsage = new HashMap<>();

        technologyUsage.put(TechnologiesStatsModel.STATS_SERVICES_EJB_STATELESS, this.countByType(EjbSessionBeanModel.class, EjbBeanBaseModel.SESSION_TYPE, "stateless"));
        technologyUsage.put(TechnologiesStatsModel.STATS_SERVICES_EJB_STATEFUL, this.countByType(EjbSessionBeanModel.class,  EjbBeanBaseModel.SESSION_TYPE, "stateful"));
        technologyUsage.put(TechnologiesStatsModel.STATS_SERVICES_EJB_MESSAGEDRIVEN, this.countByType(EjbMessageDrivenModel.class));
        // TODO: stats.setStatsServicesEjb___(item(countByType(EjbDeploymentDescriptorModel.class)));

        // Amounts
        // For the commented, we don't have a graph representation.
        Map<ProjectModel, Integer> count = this.sum(
                this.countByType(EjbEntityBeanModel.class),
                this.countByType(JPAEntityModel.class)
        );

        // PersistenceEntityModel covers also HibernateEntityModel.
        technologyUsage.put(TechnologiesStatsModel.STATS_SERVICES_JPA_ENTITITES, count);
        technologyUsage.put(TechnologiesStatsModel.STATS_SERVICES_JPA_NAMEDQUERIES, countByType(JPANamedQueryModel.class));
        technologyUsage.put(TechnologiesStatsModel.STATS_SERVICES_JPA_PERSISTENCEUNITS, countByType(JPAPersistenceUnitModel.class));
        technologyUsage.put(TechnologiesStatsModel.STATS_SERVICES_RMI_SERVICES, countByType(RMIServiceModel.class));

        technologyUsage.put(TechnologiesStatsModel.STATS_SERVICES_HIBERNATE_CONFIGURATIONFILES, countByType(HibernateConfigurationFileModel.class));
        technologyUsage.put(TechnologiesStatsModel.STATS_SERVICES_HIBERNATE_ENTITIES, countByType(HibernateEntityModel.class));
        technologyUsage.put(TechnologiesStatsModel.STATS_SERVICES_HIBERNATE_MAPPINGFILES, countByType(HibernateMappingFileModel.class));
        technologyUsage.put(TechnologiesStatsModel.STATS_SERVICES_HIBERNATE_SESSIONFACTORIES, countByType(HibernateSessionFactoryModel.class));

        technologyUsage.put(TechnologiesStatsModel.STATS_SERVERRESOURCES_DB_JDBCDATASOURCES, this.countByType(DataSourceModel.class, new HashMap<String, Serializable>(){{
            put(DataSourceModel.IS_XA, false);
        }}));

        technologyUsage.put(TechnologiesStatsModel.STATS_SERVERRESOURCES_DB_XAJDBCDATASOURCES, this.countByType(DataSourceModel.class, new HashMap<String, Serializable>(){{
            put(DataSourceModel.IS_XA, true);
        }}));

        technologyUsage.put(TechnologiesStatsModel.STATS_SERVICES_HTTP_JAX_RS, this.countByType(JaxRSWebServiceModel.class));
        technologyUsage.put(TechnologiesStatsModel.STATS_SERVICES_HTTP_JAX_WS, this.countByType(JaxWSWebServiceModel.class));

        technologyUsage.put(TechnologiesStatsModel.STATS_SERVERRESOURCES_MSG_JMS_QUEUES, this.countByType(JmsDestinationModel.class,
                JmsDestinationModel.DESTINATION_TYPE, JmsDestinationType.QUEUE.name()));
        technologyUsage.put(TechnologiesStatsModel.STATS_SERVERRESOURCES_MSG_JMS_TOPICS, this.countByType(JmsDestinationModel.class,
                JmsDestinationModel.DESTINATION_TYPE, JmsDestinationType.TOPIC.name()));
        technologyUsage.put(TechnologiesStatsModel.STATS_SERVERRESOURCES_MSG_JMS_CONNECTIONFACTORIES, this.countByType(JmsConnectionFactoryModel.class));

        //stats.setStatsServerResourcesSecurityRealms(item(countByType(.class)));
        technologyUsage.put(TechnologiesStatsModel.STATS_SERVERRESOURCES_JNDI_TOTALENTRIES, this.countByType(JNDIResourceModel.class));

        // Not sure how to get this number. Maybe JavaClassFileModel.getJavaClass() ?
        // TODO: Fix this
        // result.put(TechnologiesStatsModel.STATS_JAVA_CLASSES_ORIGINAL, (int) countJavaClassesOriginal()));
        technologyUsage.put(TechnologiesStatsModel.STATS_JAVA_CLASSES_TOTAL, this.countByType(JavaClassModel.class));

        // We are not able to tell which of the jars are original. We can substract known opensource libs.
        technologyUsage.put(TechnologiesStatsModel.STATS_JAVA_JARS_ORIGINAL, this.diff(
                this.countByType(JarArchiveModel.class),
                this.countByType(IdentifiedArchiveModel.class))
        );
        technologyUsage.put(TechnologiesStatsModel.STATS_JAVA_JARS_TOTAL, this.countByType(JarArchiveModel.class));


        return this.groupByProjectModel(technologyUsage);
    }

    protected Map<ProjectModel, Map<String, Integer>> groupByProjectModel(Map<String, Map<ProjectModel, Integer>> groupedByTechnology)
    {
        Map<ProjectModel, Map<String, Integer>> projectBasedResult = new HashMap<>();

        groupedByTechnology.entrySet().forEach(technologyMap -> {
            technologyMap.getValue().entrySet().forEach(projectTechCount -> {
                ProjectModel project = projectTechCount.getKey();

                if (!projectBasedResult.containsKey(project))
                {
                    projectBasedResult.put(project, new HashMap<>());
                }

                projectBasedResult.get(project).put(technologyMap.getKey(), projectTechCount.getValue());
            });
        });

        return projectBasedResult;
    }

    protected Map<ProjectModel, Integer> sum(Map<ProjectModel, Integer> a, Map<ProjectModel, Integer> b) {
        Map<ProjectModel, Integer> result = new HashMap<>();

        a.entrySet().forEach(keyValuePair -> result.put(keyValuePair.getKey(), keyValuePair.getValue()));
        b.entrySet().forEach(keyValuePair -> result.put(
                keyValuePair.getKey(),
                result.getOrDefault(keyValuePair.getKey(), 0) + keyValuePair.getValue()
        ));

        return result;
    }

    protected Map<ProjectModel, Integer> diff(Map<ProjectModel, Integer> a, Map<ProjectModel, Integer> b) {
        Map<ProjectModel, Integer> result = new HashMap<>();

        a.entrySet().forEach(keyValuePair -> result.put(keyValuePair.getKey(), keyValuePair.getValue()));
        b.entrySet().forEach(keyValuePair -> result.put(
                keyValuePair.getKey(),
                result.getOrDefault(keyValuePair.getKey(), keyValuePair.getValue()) - keyValuePair.getValue()
        ));

        return result;
    }

    private <T extends WindupVertexFrame> Map<ProjectModel, Integer> countByType(Class<T> clazz)
    {
        return countByType(clazz, null);
    }

    private <T extends WindupVertexFrame> Map<ProjectModel, Integer> countByType(Class<T> clazz, String propName, Serializable value)
    {
        ///LOG.info("Counting: Frame class == " + clazz.getSimpleName() + " && " + propName + " == " + value);
        return countByType(clazz, propName == null ? null : new HashMap<String, Serializable>(){{put(propName, value);}});
    }

    private <T extends WindupVertexFrame> Map<ProjectModel, Integer> countByType(Class<T> clazz, Map<String, Serializable> props)
    {
        FramedGraphQuery query = this.getGraphContext().getQuery().type(clazz);

        if (props != null) {
            for (Map.Entry<String, Serializable> prop : props.entrySet()) {
                String propName = prop.getKey();
                Serializable value = prop.getValue();
                if (value == null)
                    query = query.has(propName);
                else
                    query = query.has(propName, value);
            }
        }

        Map<ProjectModel, Integer> projectCount = new HashMap<>();
        Iterable<T> vertices = query.vertices(clazz);

        for (T vertex : vertices) {
            if (vertex instanceof BelongsToProject) {
                for (ProjectModel projectModel : this.projectModelToRootProjectModel.keySet()) {
                    if (((BelongsToProject)vertex).belongsToProject(projectModel)) {
                        ProjectModel rootProjectModel = this.projectModelToRootProjectModel.get(projectModel);
                        projectCount.put(rootProjectModel, projectCount.getOrDefault(rootProjectModel, 0) + 1);
                        // TODO: Could file belong to multiple project models?
                        // break;
                    }
                }
            } else {
                String errorMessage = "Not instance of " +
                        BelongsToProject.class.getName() +
                        "\n" +
                        clazz.getName();

                LOG.warning(errorMessage);
            }
        }

        LOG.info("Counted: Frame class == " + clazz.getSimpleName() + " && " + (props == null ? "no" : props.size()));

        return projectCount;
    }

    protected Map<ProjectModel, Map<String, Integer>> countFilesBySuffix()
    {
        Map<ProjectModel, Map<String, Integer>> result = new HashMap<>();

        Iterable<FileModel> files = this.getGraphContext().getQuery()
                .type(FileModel.class)
                .hasNot(FileModel.IS_DIRECTORY, true)
                .vertices(FileModel.class);

        StreamSupport.stream(files.spliterator(), false)
                .forEach((FileModel file) -> {
                    String suffix = StringUtils.substringAfterLast(file.getFileName(), ".");

                    if (suffix.isEmpty())
                    {
                        return;
                    }

                    ProjectModel projectModel = file.getProjectModel();
                    ProjectModel rootProjectModel = this.projectModelToRootProjectModel.get(projectModel);
                    Map<String, Integer> suffixToCount;

                    if (rootProjectModel == null)
                    {
                        throw new RuntimeException("RootProjectModel null");
                    }
                    else
                    {
                        if (!result.containsKey(rootProjectModel))
                        {
                            result.put(rootProjectModel, new HashMap<>());
                        }

                        suffixToCount = result.get(rootProjectModel);
                    }

                    Integer val = suffixToCount.get(suffix);

                    if (val == null)
                    {
                        suffixToCount.put(suffix, 1);
                    }
                    else
                    {
                        suffixToCount.put(suffix, val + 1);
                    }
                });

        return result;
    }

    /*
     * Shortcut methods when only the qty is needed.
     */
    private GeneralStatsItemModel item(int i) {
        return this.getGraphContext().create(GeneralStatsItemModel.class).setQuantity(i);
    }

    private GeneralStatsItemModel item(
            String key, String label, Class<? extends WindupVertexFrame> type, Map<String, String> props
    ) {
        GeneralStatsItemModel item = this.getGraphContext().create(GeneralStatsItemModel.class).setKey(key).setLabel(label);
        long qty = countGraphVertices(type, props);
        item.setQuantity((int) qty);
        return item;
    }

    private long countGraphVertices(Class<? extends WindupVertexFrame> clazz, Map<String, String> props) {
        if (clazz == null)
            throw new IllegalArgumentException("Frame type must be set (was null).");

        LOG.info("Counting: Frame class == " + clazz.getSimpleName() + " && " + CollectionUtils.size(props) + " props.");
        FramedGraphQuery query = this.getGraphContext().getQuery().type(clazz);
        //props.entrySet().forEach( e -> {
        if (props != null)
            for (Map.Entry<String, String> e : props.entrySet()) {
                String key = e.getKey();
                if (key != null && !key.isEmpty())
                    if (e.getValue() == null)
                        query = query.has(key);
                    else
                        query = query.has(key, e.getValue());
            }
        long count = this.count(query.vertices());
        LOG.info(" ==> " + count);
        return count;
    }

    // Methods for individual statistic items
    private long countJavaClassesOriginal()
    {
        //new Pipeline<Vertex, Vertex>().
        Iterable<Vertex> startVertices = new TypeAwareFramedGraphQuery(this.getGraphContext().getFramed()).type(JavaClassModel.class).vertices();
        Pipeline<Vertex, Vertex> pipeline = new Pipeline<Vertex, Vertex>();
        pipeline.addPipe(new StartPipe(startVertices));
        final OutPipe outPipe = new OutPipe(JavaClassModel.DECOMPILED_SOURCE);
        // The BackFilterPipe needs to wrap all pipes which it "go back before".
        // This means ...out(...).back(1);
        pipeline.addPipe(new BackFilterPipe(outPipe));
        return pipeline.count();
    }
}
