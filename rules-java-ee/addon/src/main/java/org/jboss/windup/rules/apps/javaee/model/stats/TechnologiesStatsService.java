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
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.javaee.model.EjbBeanBaseModel;
import org.jboss.windup.rules.apps.javaee.model.EjbEntityBeanModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.model.JPANamedQueryModel;
import org.jboss.windup.rules.apps.javaee.model.JPAPersistenceUnitModel;
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
        
        stats.setStatsFilesByTypeJavaPercent(suffixToCount.getOrDefault(".class", 0) + suffixToCount.getOrDefault(".java", 0));
        stats.setStatsFilesByTypeJsPercent(suffixToCount.getOrDefault(".js", 0));
        stats.setStatsFilesByTypeHtmlPercent(suffixToCount.getOrDefault(".html", 0));
        stats.setStatsFilesByTypeCssPercent(suffixToCount.getOrDefault(".css", 0));
        stats.setStatsFilesByTypeXmlPercent(suffixToCount.getOrDefault(".xml", 0));
        stats.setStatsFilesByTypeFmtPercent(suffixToCount.getOrDefault(".fmt", 0));
        
        
        // Amounts
        stats.setStatsServicesEjbStateless((int) countByType(EjbSessionBeanModel.class, EjbBeanBaseModel.SESSION_TYPE, "stateless"));
        stats.setStatsServicesEjbStateful((int) countByType(EjbSessionBeanModel.class,  EjbBeanBaseModel.SESSION_TYPE, "stateful"));
        stats.setStatsServicesEjbMessageDriven((int) countByType(EjbMessageDrivenModel.class));
        
        stats.setStatsServicesJpaEntitites((int) countByType(EjbEntityBeanModel.class));
        stats.setStatsServicesJpaNamedQueries((int) countByType(JPANamedQueryModel.class));
        stats.setStatsServicesJpaPersistenceUnits((int) countByType(JPAPersistenceUnitModel.class));
        
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
            String suffix = StringUtils.substringAfterLast(file.getFileName(), ".");
            Integer val = suffixToCount.get(suffix);
            if (val == null)
                suffixToCount.put(suffix, 1);
            else
                suffixToCount.put(suffix, val +1);
        });
        return suffixToCount;
    }
    
    private static Map<String, Integer> countFilesShareBySuffix(){
        Map<String, Integer> shares = new HashMap<>();
        // TODO
        return shares;
    }
    
}
