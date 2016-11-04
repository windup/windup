package org.jboss.windup.rules.apps.javaee.model.stats;

import com.tinkerpop.blueprints.Vertex;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;

import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.pipes.filter.BackFilterPipe;
import com.tinkerpop.pipes.transform.OutPipe;
import com.tinkerpop.pipes.util.Pipeline;
import com.tinkerpop.pipes.util.StartPipe;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.model.JarArchiveModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.model.EjbBeanBaseModel;
import org.jboss.windup.rules.apps.javaee.model.EjbEntityBeanModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.model.JPANamedQueryModel;
import org.jboss.windup.rules.apps.javaee.model.JPAPersistenceUnitModel;
import org.jboss.windup.rules.apps.javaee.model.JaxRSWebServiceModel;
import org.jboss.windup.rules.apps.javaee.model.JaxWSWebServiceModel;
import org.jboss.windup.rules.apps.javaee.model.JmsConnectionFactoryModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationType;
import org.jboss.windup.util.Logging;

/**
 * Functionality for the Technologies Report.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class TechnologiesStatsService extends GraphService<TechnologiesStatsModel>
{
    private final static Logger LOG = Logging.get(TechnologiesStatsService.class);

    public TechnologiesStatsService(GraphContext context)
    {
        super(context, TechnologiesStatsModel.class);
    }

    /**
     * Compute the stats for this execution.
     */
    public TechnologiesStatsModel computeStats()
    {
        TechnologiesStatsModel stats = this.create();
        stats.setComputed(new Date());

        // Files type share
        Map<String, Integer> suffixToCount = countFilesBySuffix();
        Map<String, Integer> fileTypeShares = countFilesShareBySuffix(suffixToCount);
        // This will need to filter out archives.

        stats.setStatsFilesByTypeJavaPercent(item(fileTypeShares.getOrDefault("class", 0) + fileTypeShares.getOrDefault("java", 0)));
        stats.setStatsFilesByTypeJsPercent(item(fileTypeShares.getOrDefault("js", 0)));
        stats.setStatsFilesByTypeHtmlPercent(item(fileTypeShares.getOrDefault("html", 0)));
        stats.setStatsFilesByTypeCssPercent(item(fileTypeShares.getOrDefault("css", 0)));
        stats.setStatsFilesByTypeXmlPercent(item(fileTypeShares.getOrDefault("xml", 0)));
        stats.setStatsFilesByTypeFmtPercent(item(fileTypeShares.getOrDefault("fmt", 0)));


        // Amounts
        // For the commented, we don't have a graph representation.
        stats.setStatsServicesEjbStateless(item(countByType(EjbSessionBeanModel.class, EjbBeanBaseModel.SESSION_TYPE, "stateless")));
        stats.setStatsServicesEjbStateful(item(countByType(EjbSessionBeanModel.class,  EjbBeanBaseModel.SESSION_TYPE, "stateful")));
        stats.setStatsServicesEjbMessageDriven(item(countByType(EjbMessageDrivenModel.class)));

        stats.setStatsServicesJpaEntitites(item(countByType(EjbEntityBeanModel.class)));
        stats.setStatsServicesJpaNamedQueries(item(countByType(JPANamedQueryModel.class)));
        stats.setStatsServicesJpaPersistenceUnits(item(countByType(JPAPersistenceUnitModel.class)));
        //stats.setStatsServicesRmiServices(item(countByType(.class)));

        //stats.setStatsServerResourcesDbJdbcDatasources(item(countByType(.class)));
        //stats.setStatsServerResourcesDbXaJdbcDatasources(item(countByType(.class)));

        stats.setStatsServicesHttpJaxRs(item(countByType(JaxRSWebServiceModel.class)));
        stats.setStatsServicesHttpJaxWs(item(countByType(JaxWSWebServiceModel.class)));

        stats.setStatsServerResourcesMsgJmsQueues(item(countByType(JmsDestinationModel.class, JmsDestinationModel.DESTINATION_TYPE, JmsDestinationType.QUEUE.name())));
        stats.setStatsServerResourcesMsgJmsTopics(item(countByType(JmsDestinationModel.class, JmsDestinationModel.DESTINATION_TYPE, JmsDestinationType.TOPIC.name())));
        stats.setStatsServerResourcesMsgJmsConnectionFactories(item(countByType(JmsConnectionFactoryModel.class)));

        //stats.setStatsServerResourcesSecurityRealms(item(countByType(.class)));
        stats.setStatsServerResourcesJndiTotalEntries(item(countByType(JNDIResourceModel.class)));

        // Not sure how to get this number. Maybe JavaClassFileModel.getJavaClass() ?
        stats.setStatsJavaClassesOriginal(item((int) countJavaClassesOriginal()));
        stats.setStatsJavaClassesTotal(item(countByType(JavaClassModel.class)));
        // We are not able to tell which of the jars are original. We can substract known opensource libs.
        stats.setStatsJavaJarsOriginal(item(countByType(JarArchiveModel.class) - countByType(IdentifiedArchiveModel.class)));
        stats.setStatsJavaJarsTotal(item(countByType(JarArchiveModel.class)));

        this.commit();
        return stats;
    }


    private <T extends WindupVertexFrame> int countByType(Class<T> clazz)
    {
        return countByType(clazz, null, null);
    }

    private <T extends WindupVertexFrame> int countByType(Class<T> clazz, String propName, String value)
    {
        LOG.info("Counting: Frame class == " + clazz.getSimpleName() + " && " + propName + " == " + value);
        FramedGraphQuery query = this.getGraphContext().getQuery().type(clazz);
        if (propName != null && !propName.isEmpty())
            if (value == null)
                query = query.has(propName);
            else
                query = query.has(propName, value);

        long count = this.count(query.vertices());
        LOG.info(" ==> " + count);
        return (int) count;
    }

    private Map<String, Integer> countFilesBySuffix()
    {
        Map<String, Integer> suffixToCount = new HashMap<>();
        Iterable<FileModel> files = this.getGraphContext().getQuery()
                .type(FileModel.class)
                .hasNot(FileModel.IS_DIRECTORY, true)
                .vertices(FileModel.class);

        // TODO this just takes any file in the graph. Need to resctrict to project files.
        files.forEach( (FileModel file) -> {
            String suffix = StringUtils.substringAfterLast(file.getFileName(), ".");
            if (suffix.isEmpty())
                return;
            Integer val = suffixToCount.get(suffix);
            if (val == null)
                suffixToCount.put(suffix, 1);
            else
                suffixToCount.put(suffix, val +1);
        });
        return suffixToCount;
    }

    private static Map<String, Integer> countFilesShareBySuffix(Map<String, Integer> suffixToCount){
        int sum = suffixToCount.entrySet().stream().mapToInt(e -> e.getValue()).sum();
        System.out.println("SUM: " + sum);
        Map<String, Integer> shares = suffixToCount.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey(),
                e -> e.getValue() * 100 / sum
        ));
        return shares;
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
