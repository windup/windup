package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.MapInAdjacentProperties;
import org.jboss.windup.graph.model.TypeValue;

import java.util.Map;

/**
 * Contains EJB Message Driven model information and related data.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(EjbMessageDrivenModel.TYPE)
public interface EjbMessageDrivenModel extends EjbBeanBaseModel {

    String TYPE = "EjbMessageDrivenModel";
    String DESTINATION = "destination";
    String THREAD_POOL = "threadPool";
    String TX_TIMEOUTS = "txTimeouts";


    /**
     * Contains the destination address, typically a JMS queue or topic
     */
    @Adjacency(label = DESTINATION, direction = Direction.IN)
    JmsDestinationModel getDestination();

    /**
     * Contains the destination address, typically a JMS queue or topic
     */
    @Adjacency(label = DESTINATION, direction = Direction.IN)
    void setDestination(JmsDestinationModel destination);

    /**
     * References the Deployment Descriptor containing EJB.
     */
    @Adjacency(label = EjbDeploymentDescriptorModel.MESSAGE_DRIVEN, direction = Direction.IN)
    public EjbDeploymentDescriptorModel getEjbDeploymentDescriptor();


    /**
     * Timeouts for each method pattern in seconds, * is wildcard
     */
    @MapInAdjacentProperties(label = TX_TIMEOUTS)
    Map<String, Integer> getTxTimeouts();

    /**
     * Timeouts for each method pattern, * is wildcard
     */
    @MapInAdjacentProperties(label = TX_TIMEOUTS)
    void setTxTimeouts(Map<String, Integer> map);

    /**
     * References the thread pool, if defined.
     */
    @Adjacency(label = THREAD_POOL, direction = Direction.OUT)
    ThreadPoolModel getThreadPool();

    /**
     * References the thread pool, if defined.
     */
    @Adjacency(label = THREAD_POOL, direction = Direction.OUT)
    void setThreadPool(ThreadPoolModel threadPool);
}
