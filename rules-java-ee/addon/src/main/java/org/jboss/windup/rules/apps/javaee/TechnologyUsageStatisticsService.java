package org.jboss.windup.rules.apps.javaee;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.model.BelongsToProject;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.model.stats.GeneralStatsItemModel;
import org.jboss.windup.rules.apps.javaee.model.stats.TechnologyUsageStatisticsModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.pipes.filter.BackFilterPipe;
import com.tinkerpop.pipes.transform.OutPipe;
import com.tinkerpop.pipes.util.Pipeline;
import com.tinkerpop.pipes.util.StartPipe;
import org.jboss.windup.util.Logging;

/**
 * Provides CRUD methods for accessing the {@link TechnologyUsageStatisticsModel} vertices.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TechnologyUsageStatisticsService extends GraphService<TechnologyUsageStatisticsModel>
{
    private static Logger LOG = Logging.get(TechnologyUsageStatisticsService.class);

    /**
     * Creates an instance of this service.
     */
    public TechnologyUsageStatisticsService(GraphContext context)
    {
        super(context, TechnologyUsageStatisticsModel.class);
    }

    public TechnologyUsageStatisticsModel getOrCreate(ProjectModel projectModel, String technologyName)
    {
        Iterable<TechnologyUsageStatisticsModel> byName = findAllByProperty(TechnologyUsageStatisticsModel.NAME, technologyName);
        TechnologyUsageStatisticsModel result = null;

        for (TechnologyUsageStatisticsModel candidate : byName)
        {
            if (candidate.getProjectModel().equals(projectModel))
            {
                result = candidate;
                break;
            }
        }

        if (result == null)
        {
            result = create();
            result.setComputed(new Date());
            result.setProjectModel(projectModel);
            result.setName(technologyName);
            result.setOccurrenceCount(0);
        }
        return result;
    }

    public Map<ProjectModel, Map<String, Integer>> countTechnologiesUsage()
    {
        Map<String, Map<ProjectModel, Integer>> technologyUsage = new HashMap<>();

        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVICES_EJB_STATELESS, this.countByType(EjbSessionBeanModel.class,
        // EjbBeanBaseModel.SESSION_TYPE, "stateless"));
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVICES_EJB_STATEFUL, this.countByType(EjbSessionBeanModel.class,
        // EjbBeanBaseModel.SESSION_TYPE, "stateful"));
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVICES_EJB_MESSAGEDRIVEN, this.countByType(EjbMessageDrivenModel.class));
        // // TODO: stats.setStatsServicesEjb___(item(countByType(EjbDeploymentDescriptorModel.class)));
        //
        // // Amounts
        // // For the commented, we don't have a graph representation.
        // Map<ProjectModel, Integer> count = this.sum(
        // this.countByType(EjbEntityBeanModel.class),
        // this.countByType(JPAEntityModel.class)
        // );
        //
        // // PersistenceEntityModel covers also HibernateEntityModel.
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVICES_JPA_ENTITITES, count);
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVICES_JPA_NAMEDQUERIES, countByType(JPANamedQueryModel.class));
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVICES_JPA_PERSISTENCEUNITS, countByType(JPAPersistenceUnitModel.class));
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVICES_RMI_SERVICES, countByType(RMIServiceModel.class));
        //
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVICES_HIBERNATE_CONFIGURATIONFILES,
        // countByType(HibernateConfigurationFileModel.class));
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVICES_HIBERNATE_ENTITIES, countByType(HibernateEntityModel.class));
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVICES_HIBERNATE_MAPPINGFILES, countByType(HibernateMappingFileModel.class));
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVICES_HIBERNATE_SESSIONFACTORIES,
        // countByType(HibernateSessionFactoryModel.class));
        //
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVERRESOURCES_DB_JDBCDATASOURCES, this.countByType(DataSourceModel.class, new
        // HashMap<String, Serializable>(){{
        // put(DataSourceModel.IS_XA, false);
        // }}));
        //
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVERRESOURCES_DB_XAJDBCDATASOURCES, this.countByType(DataSourceModel.class, new
        // HashMap<String, Serializable>(){{
        // put(DataSourceModel.IS_XA, true);
        // }}));
        //
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVICES_HTTP_JAX_RS, this.countByType(JaxRSWebServiceModel.class));
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVICES_HTTP_JAX_WS, this.countByType(JaxWSWebServiceModel.class));
        //
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVERRESOURCES_MSG_JMS_QUEUES, this.countByType(JmsDestinationModel.class,
        // JmsDestinationModel.DESTINATION_TYPE, JmsDestinationType.QUEUE.name()));
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVERRESOURCES_MSG_JMS_TOPICS, this.countByType(JmsDestinationModel.class,
        // JmsDestinationModel.DESTINATION_TYPE, JmsDestinationType.TOPIC.name()));
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVERRESOURCES_MSG_JMS_CONNECTIONFACTORIES,
        // this.countByType(JmsConnectionFactoryModel.class));
        //
        // //stats.setStatsServerResourcesSecurityRealms(item(countByType(.class)));
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_SERVERRESOURCES_JNDI_TOTALENTRIES, this.countByType(JNDIResourceModel.class));
        //
        // // Not sure how to get this number. Maybe JavaClassFileModel.getJavaClass() ?
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_JAVA_CLASSES_ORIGINAL, this.countJavaClassesOriginal());
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_JAVA_CLASSES_TOTAL, this.countByType(JavaClassModel.class));
        //
        // // We are not able to tell which of the jars are original. We can substract known opensource libs.
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_JAVA_JARS_ORIGINAL, this.diff(
        // this.countByType(JarArchiveModel.class),
        // this.countByType(IdentifiedArchiveModel.class))
        // );
        // technologyUsage.put(TechnologyUsageStatisticsModel.STATS_JAVA_JARS_TOTAL, this.countByType(JarArchiveModel.class));

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

    protected Map<ProjectModel, Integer> sum(Map<ProjectModel, Integer> a, Map<ProjectModel, Integer> b)
    {
        Map<ProjectModel, Integer> result = new HashMap<>();

        a.entrySet().forEach(keyValuePair -> result.put(keyValuePair.getKey(), keyValuePair.getValue()));
        b.entrySet().forEach(keyValuePair -> result.put(
                    keyValuePair.getKey(),
                    result.getOrDefault(keyValuePair.getKey(), 0) + keyValuePair.getValue()));

        return result;
    }

    protected Map<ProjectModel, Integer> diff(Map<ProjectModel, Integer> a, Map<ProjectModel, Integer> b)
    {
        Map<ProjectModel, Integer> result = new HashMap<>();

        a.entrySet().forEach(keyValuePair -> result.put(keyValuePair.getKey(), keyValuePair.getValue()));
        b.entrySet().forEach(keyValuePair -> result.put(
                    keyValuePair.getKey(),
                    result.getOrDefault(keyValuePair.getKey(), keyValuePair.getValue()) - keyValuePair.getValue()));

        return result;
    }

    private <T extends WindupVertexFrame> Map<ProjectModel, Integer> countByType(Class<T> clazz)
    {
        return countByType(clazz, null);
    }

    private <T extends WindupVertexFrame> Map<ProjectModel, Integer> countByType(Class<T> clazz, String propName, Serializable value)
    {
        /// LOG.info("Counting: Frame class == " + clazz.getSimpleName() + " && " + propName + " == " + value);
        return countByType(clazz, propName == null ? null : new HashMap<String, Serializable>()
        {
            {
                put(propName, value);
            }
        });
    }

    private <T extends WindupVertexFrame> Map<ProjectModel, Integer> countByType(Class<T> clazz, Map<String, Serializable> props)
    {
        FramedGraphQuery query = this.getGraphContext().getQuery().type(clazz);

        if (props != null)
        {
            for (Map.Entry<String, Serializable> prop : props.entrySet())
            {
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

        for (T vertex : vertices)
        {
            if (vertex instanceof BelongsToProject)
            {
                for (ProjectModel projectModel : ((BelongsToProject) vertex).getRootProjectModels())
                {
                    projectCount.put(projectModel, projectCount.getOrDefault(projectModel, 0) + 1);
                }
            }
            else
            {
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

    public Map<ProjectModel, Map<String, Integer>> countFilesBySuffix()
    {
        Map<ProjectModel, Map<String, Integer>> result = new HashMap<>();

        Iterable<FileModel> files = this.getGraphContext().getQuery()
                    .type(FileModel.class)
                    .hasNot(FileModel.IS_DIRECTORY, true)
                    .vertices(FileModel.class);

        StreamSupport.stream(files.spliterator(), false)
                    .forEach((FileModel file) -> {
                        String suffix = StringUtils.substringAfterLast(file.getFileName(), ".");

                        if (suffix.isEmpty() || file.isWindupGenerated())
                        {
                            return;
                        }

                        ProjectModel projectModel = file.getProjectModel();
                        if (projectModel == null)
                        {
                            return;
                        }

                        ProjectModel rootProjectModel = projectModel.getRootProjectModel();
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
    private GeneralStatsItemModel item(int i)
    {
        return this.getGraphContext().create(GeneralStatsItemModel.class).setQuantity(i);
    }

    private GeneralStatsItemModel item(
                String key, String label, Class<? extends WindupVertexFrame> type, Map<String, String> props)
    {
        GeneralStatsItemModel item = this.getGraphContext().create(GeneralStatsItemModel.class).setKey(key).setLabel(label);
        long qty = countGraphVertices(type, props);
        item.setQuantity((int) qty);
        return item;
    }

    private long countGraphVertices(Class<? extends WindupVertexFrame> clazz, Map<String, String> props)
    {
        if (clazz == null)
            throw new IllegalArgumentException("Frame type must be set (was null).");

        LOG.info("Counting: Frame class == " + clazz.getSimpleName() + " && " + CollectionUtils.size(props) + " props.");
        FramedGraphQuery query = this.getGraphContext().getQuery().type(clazz);
        // props.entrySet().forEach( e -> {
        if (props != null)
            for (Map.Entry<String, String> e : props.entrySet())
            {
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
    private Map<ProjectModel, Integer> countJavaClassesOriginal()
    {
        // TODO: Fix this
        // new Pipeline<Vertex, Vertex>().
        Iterable<Vertex> startVertices = new TypeAwareFramedGraphQuery(this.getGraphContext().getFramed()).type(JavaClassModel.class).vertices();
        Pipeline<Vertex, Vertex> pipeline = new Pipeline<>();
        pipeline.addPipe(new StartPipe(startVertices));
        final OutPipe outPipe = new OutPipe(JavaClassModel.DECOMPILED_SOURCE);
        // The BackFilterPipe needs to wrap all pipes which it "go back before".
        // This means ...out(...).back(1);
        pipeline.addPipe(new BackFilterPipe(outPipe));

        Map<ProjectModel, Integer> map = new HashMap<>();

        Iterable<JavaClassModel> javaClassModels = this.getGraphContext().getFramed().frameVertices(pipeline, JavaClassModel.class);

        javaClassModels.forEach(item -> {
            FileModel fileModel = item.getDecompiledSource();

            if (fileModel == null)
            {
                LOG.warning("Unexpected fileModel null");
                return;
            }

            ProjectModel projectModel = fileModel.getProjectModel();

            if (projectModel == null)
            {
                LOG.warning("Unexpected projectModel null");
                return;
            }

            ProjectModel rootProjectModel = projectModel.getRootProjectModel();
            map.put(rootProjectModel, map.getOrDefault(rootProjectModel, 0) + 1);
        });

        return map;
    }

}
