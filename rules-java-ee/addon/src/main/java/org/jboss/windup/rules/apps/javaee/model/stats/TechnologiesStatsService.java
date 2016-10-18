package org.jboss.windup.rules.apps.javaee.model.stats;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.javaee.model.EjbBeanBaseModel;
import org.jboss.windup.rules.apps.javaee.model.EjbEntityBeanModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
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
        
        stats.setStatsServicesEjbStateless(countByType(EjbSessionBeanModel.class, EjbBeanBaseModel.SESSION_TYPE, "stateless"));
        stats.setStatsServicesEjbStateful(countByType(EjbSessionBeanModel.class));
        stats.setStatsServicesEjbMessageDriven(countByType(EjbMessageDrivenModel.class));
        
        stats.setStatsServicesJpaEntitites(countByType(EjbEntityBeanModel.class));
        
        return stats;
    }

    
    private <T extends WindupVertexFrame> int countByType(Class<T> clazz)
    {
        return countByTypeValue(getTypeValueForModel(clazz));
    }

    private <T extends WindupVertexFrame> int countByType(Class<T> clazz, String propName, String value)
    {
        return countByTypeValue(getTypeValueForModel(clazz), propName, value);
    }
    
    private int countByTypeValue(String typeValue)
    {
        return countByTypeValue(typeValue, null, null);
    }
    
    private int countByTypeValue(String typeValue, String propName, String value)
    {
        GremlinPipeline<Vertex, Integer> pipeline = new GremlinPipeline<>();
        pipeline.V().has(WindupVertexFrame.TYPE_PROP, typeValue);
        if (propName != null && !propName.isEmpty())
            if (value == null)
                pipeline.has(propName);
            else
                pipeline.has(propName, value);
        pipeline.count();
        if (pipeline.hasNext())
        {
            Integer count = pipeline.next();
            LOG.info("@TypeValue: " + typeValue + "  Count: " + count);
            return count;
        }
        return new Random().nextInt();
    }
    
    private <T extends WindupVertexFrame> String getTypeValueForModel(Class<T> clazz) throws IllegalArgumentException
    {
        TypeValue ann = clazz.getAnnotation(TypeValue.class);
        if (ann == null)
            throw new IllegalArgumentException("Class missing @" + TypeValue.class.getSimpleName() + ": " + clazz.getName() );
        String typeValue = ann.value();
        return typeValue;
    }
    
}
