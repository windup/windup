package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata related to Hibernate Session Factories.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
@TypeValue(HibernateSessionFactoryModel.TYPE)
public interface HibernateSessionFactoryModel extends WindupVertexFrame
{

    public static final String TYPE = "HibernateSessionFactory";

}
