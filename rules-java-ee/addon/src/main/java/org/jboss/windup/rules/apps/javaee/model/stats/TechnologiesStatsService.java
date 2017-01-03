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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.CollectionUtils;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.model.WindupVertexFrame;
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
 */
public class TechnologiesStatsService extends GraphService<TechnologiesStatsModel>
{
    private final static Logger LOG = Logging.get(TechnologiesStatsService.class);

    private Set<ProjectModel> projects;
    private Map<ProjectModel, ProjectModel> projectModelToRootProjectModel;

    public TechnologiesStatsService(GraphContext context)
    {
        this(context, Collections.emptySet());
    }

    public TechnologiesStatsService(GraphContext context, Set<ProjectModel> projects)
    {
        super(context, TechnologiesStatsModel.class);
        this.projects = projects;
    }

    public TechnologiesStatsService(GraphContext context, Set<ProjectModel> projects, Map<ProjectModel, ProjectModel> projectToRootProject)
    {
        super(context, TechnologiesStatsModel.class);
        this.projects = projects;
        this.projectModelToRootProjectModel = projectToRootProject;
    }

    protected void countFilesByType(TechnologiesStatsModel stats)
    {
        // Files type share
        Map<String, Integer> suffixToCount = new HashMap<>(); // = countFilesBySuffix();

        // This will need to filter out archives.
        stats.setStatsFilesByTypeJava(item(suffixToCount.getOrDefault("class", 0) + suffixToCount.getOrDefault("java", 0)));
        stats.setStatsFilesByTypeJs(item(suffixToCount.getOrDefault("js", 0)));
        stats.setStatsFilesByTypeHtml(item(suffixToCount.getOrDefault("html", 0)));
        stats.setStatsFilesByTypeCss(item(suffixToCount.getOrDefault("css", 0)));
        stats.setStatsFilesByTypeXml(item(suffixToCount.getOrDefault("xml", 0)));
        stats.setStatsFilesByTypeFmt(item(suffixToCount.getOrDefault("fmt", 0)));
    }

    /**
     * Compute the stats for this execution.
     */
    public TechnologiesStatsModel computeStats()
    {
        // TODO: This would deserve refactoring to key-value pair

        TechnologiesStatsModel stats = this.create();
        stats.setComputed(new Date());

        this.countFilesByType(stats);

        Map<String, Map<ProjectModel, Integer>> result = new HashMap<>();

        result.put(TechnologiesStatsModel.STATS_SERVICES_EJB_STATELESS, this.countByType(EjbSessionBeanModel.class, EjbBeanBaseModel.SESSION_TYPE, "stateless"));
        result.put(TechnologiesStatsModel.STATS_SERVICES_EJB_STATEFUL, this.countByType(EjbSessionBeanModel.class,  EjbBeanBaseModel.SESSION_TYPE, "stateful"));
        result.put(TechnologiesStatsModel.STATS_SERVICES_EJB_MESSAGEDRIVEN, this.countByType(EjbMessageDrivenModel.class));
        // TODO: stats.setStatsServicesEjb___(item(countByType(EjbDeploymentDescriptorModel.class)));

        // Amounts
        // For the commented, we don't have a graph representation.
        Map<ProjectModel, Integer> count = this.sum(
                this.countByType(EjbEntityBeanModel.class),
                this.countByType(JPAEntityModel.class)
        );

        // PersistenceEntityModel covers also HibernateEntityModel.
        result.put(TechnologiesStatsModel.STATS_SERVICES_JPA_ENTITITES, count);
        result.put(TechnologiesStatsModel.STATS_SERVICES_JPA_NAMEDQUERIES, countByType(JPANamedQueryModel.class));
        result.put(TechnologiesStatsModel.STATS_SERVICES_JPA_PERSISTENCEUNITS, countByType(JPAPersistenceUnitModel.class));
        result.put(TechnologiesStatsModel.STATS_SERVICES_RMI_SERVICES, countByType(RMIServiceModel.class));

        result.put(TechnologiesStatsModel.STATS_SERVICES_HIBERNATE_CONFIGURATIONFILES, countByType(HibernateConfigurationFileModel.class));
        result.put(TechnologiesStatsModel.STATS_SERVICES_HIBERNATE_ENTITIES, countByType(HibernateEntityModel.class));
        result.put(TechnologiesStatsModel.STATS_SERVICES_HIBERNATE_MAPPINGFILES, countByType(HibernateMappingFileModel.class));
        result.put(TechnologiesStatsModel.STATS_SERVICES_HIBERNATE_SESSIONFACTORIES, countByType(HibernateSessionFactoryModel.class));

        result.put(TechnologiesStatsModel.STATS_SERVERRESOURCES_DB_JDBCDATASOURCES, this.countByType(DataSourceModel.class, new HashMap<String, Serializable>(){{
            put(DataSourceModel.IS_XA, false);
        }}));

        result.put(TechnologiesStatsModel.STATS_SERVERRESOURCES_DB_XAJDBCDATASOURCES, this.countByType(DataSourceModel.class, new HashMap<String, Serializable>(){{
            put(DataSourceModel.IS_XA, true);
        }}));

        result.put(TechnologiesStatsModel.STATS_SERVICES_HTTP_JAX_RS, this.countByType(JaxRSWebServiceModel.class));
        result.put(TechnologiesStatsModel.STATS_SERVICES_HTTP_JAX_WS, this.countByType(JaxWSWebServiceModel.class));

        result.put(TechnologiesStatsModel.STATS_SERVERRESOURCES_MSG_JMS_QUEUES, this.countByType(JmsDestinationModel.class,
                JmsDestinationModel.DESTINATION_TYPE, JmsDestinationType.QUEUE.name()));
        result.put(TechnologiesStatsModel.STATS_SERVERRESOURCES_MSG_JMS_TOPICS, this.countByType(JmsDestinationModel.class,
                JmsDestinationModel.DESTINATION_TYPE, JmsDestinationType.TOPIC.name()));
        result.put(TechnologiesStatsModel.STATS_SERVERRESOURCES_MSG_JMS_CONNECTIONFACTORIES, this.countByType(JmsConnectionFactoryModel.class));

        //stats.setStatsServerResourcesSecurityRealms(item(countByType(.class)));
        result.put(TechnologiesStatsModel.STATS_SERVERRESOURCES_JNDI_TOTALENTRIES, this.countByType(JNDIResourceModel.class));

        // Not sure how to get this number. Maybe JavaClassFileModel.getJavaClass() ?
//        result.put(TechnologiesStatsModel.STATS_JAVA_CLASSES_ORIGINAL, (int) countJavaClassesOriginal()));
        result.put(TechnologiesStatsModel.STATS_JAVA_CLASSES_TOTAL, this.countByType(JavaClassModel.class));

        // We are not able to tell which of the jars are original. We can substract known opensource libs.
        result.put(TechnologiesStatsModel.STATS_JAVA_JARS_ORIGINAL, this.diff(
                this.countByType(JarArchiveModel.class),
                this.countByType(IdentifiedArchiveModel.class))
        );
        result.put(TechnologiesStatsModel.STATS_JAVA_JARS_TOTAL, this.countByType(JarArchiveModel.class));

        this.commit();

        return stats;
    }

    protected Map<ProjectModel, Integer> sum(Map<ProjectModel, Integer> a, Map<ProjectModel, Integer> b) {
        Map<ProjectModel, Integer> result = new HashMap<>();

        a.entrySet().forEach(keyValuePair -> result.put(keyValuePair.getKey(), keyValuePair.getValue()));
        b.entrySet().forEach(keyValuePair -> result.put(keyValuePair.getKey(), result.getOrDefault(keyValuePair.getKey(), 0) + keyValuePair.getValue()));

        return result;
    }

    protected Map<ProjectModel, Integer> diff(Map<ProjectModel, Integer> a, Map<ProjectModel, Integer> b) {
        Map<ProjectModel, Integer> result = new HashMap<>();

        a.entrySet().forEach(keyValuePair -> result.put(keyValuePair.getKey(), keyValuePair.getValue()));
        b.entrySet().forEach(keyValuePair -> result.put(keyValuePair.getKey(), result.getOrDefault(keyValuePair.getKey(), keyValuePair.getValue()) - keyValuePair.getValue()));

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
                for (ProjectModel projectModel : this.projects) {
                    if (((BelongsToProject)vertex).belongsToProject(projectModel)) {
                        projectCount.put(projectModel, projectCount.getOrDefault(projectModel, 0) + 1);
                        break;
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

    private Map<ProjectModel, Map<String, Integer>> countFilesBySuffix()
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
