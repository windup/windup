package org.jboss.windup.rules.apps.javaee.model.stats;

import com.tinkerpop.blueprints.Element;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.model.JarArchiveModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.model.EjbBeanBaseModel;
import org.jboss.windup.rules.apps.javaee.model.EjbEntityBeanModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
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
        
        stats.setStatsFilesByTypeJavaPercent(fileTypeShares.getOrDefault("class", 0) + fileTypeShares.getOrDefault("java", 0));
        stats.setStatsFilesByTypeJsPercent(fileTypeShares.getOrDefault("js", 0));
        stats.setStatsFilesByTypeHtmlPercent(fileTypeShares.getOrDefault("html", 0));
        stats.setStatsFilesByTypeCssPercent(fileTypeShares.getOrDefault("css", 0));
        stats.setStatsFilesByTypeXmlPercent(fileTypeShares.getOrDefault("xml", 0));
        stats.setStatsFilesByTypeFmtPercent(fileTypeShares.getOrDefault("fmt", 0));
        
        
        // Amounts
        // For the commented, we don't have a graph representation.
        stats.setStatsServicesEjbStateless((int) countByType(EjbSessionBeanModel.class, EjbBeanBaseModel.SESSION_TYPE, "stateless"));
        stats.setStatsServicesEjbStateful((int) countByType(EjbSessionBeanModel.class,  EjbBeanBaseModel.SESSION_TYPE, "stateful"));
        stats.setStatsServicesEjbMessageDriven((int) countByType(EjbMessageDrivenModel.class));
        
        stats.setStatsServicesJpaEntitites((int) countByType(EjbEntityBeanModel.class));
        stats.setStatsServicesJpaNamedQueries((int) countByType(JPANamedQueryModel.class));
        stats.setStatsServicesJpaPersistenceUnits((int) countByType(JPAPersistenceUnitModel.class));
        //stats.setStatsServicesRmiServices((int) countByType(.class));
        
        //stats.setStatsServerResourcesDbJdbcDatasources((int) countByType(.class));
        //stats.setStatsServerResourcesDbXaJdbcDatasources((int) countByType(.class));
        
        stats.setStatsServicesHttpJaxRs((int) countByType(JaxRSWebServiceModel.class));
        stats.setStatsServicesHttpJaxWs((int) countByType(JaxWSWebServiceModel.class));
        
        stats.setStatsServerResourcesMsgJmsQueues((int) countByType(JmsDestinationModel.class, JmsDestinationModel.DESTINATION_TYPE, JmsDestinationType.QUEUE.name()));
        stats.setStatsServerResourcesMsgJmsTopics((int) countByType(JmsDestinationModel.class, JmsDestinationModel.DESTINATION_TYPE, JmsDestinationType.TOPIC.name()));
        stats.setStatsServerResourcesMsgJmsConnectionFactories((int) countByType(JmsConnectionFactoryModel.class));
        
        //stats.setStatsServerResourcesSecurityRealms((int) countByType(.class));
        //stats.setStatsServerResourcesJndiTotalEntries((int) countByType(.class));
        
        ///stats.setStatsJavaClassesOriginal((int) countByType(.class));
        stats.setStatsJavaClassesTotal((int) countByType(JavaClassModel.class));
        //stats.setStatsJavaJarsOriginal((int) countByType(JavaClassModel.class));
        stats.setStatsJavaJarsTotal((int) countByType(JarArchiveModel.class));
        
        this.commit();
        return stats;
    }

    
    private <T extends WindupVertexFrame> long countByType(Class<T> clazz)
    {
        return countByType(clazz, null, null);
    }

    private <T extends WindupVertexFrame> long countByType(Class<T> clazz, String propName, String value)
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
        return count;
    }

    
    private <T extends WindupVertexFrame> String getTypeValueForModel(Class<T> clazz)
    {
        return TypeAwareFramedGraphQuery.getTypeValue(clazz);
    }

    private Map<String, Integer> countFilesBySuffix()
    {
        Map<String, Integer> suffixToCount = new HashMap<>();
        Iterable<FileModel> files = this.getGraphContext().getQuery().type(FileModel.class).vertices(FileModel.class);
        /// TODO this just takes any file in the graph. Need to resctrict to project files.
        files.forEach( (FileModel file) -> {
            String suffix = StringUtils.substringAfterLast(file.getFileName(), "");
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
        Map<String, Integer> shares = suffixToCount.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey(),
                e -> e.getValue() / sum
        ));
        return shares;
    }
    
}
