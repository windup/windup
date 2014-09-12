package org.jboss.windup.rules.apps.javaee.model;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains EJB Message Driven model information and related data.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(EjbMessageDrivenModel.TYPE)
public interface EjbMessageDrivenModel extends EjbBeanBaseModel
{

    public static final String TYPE = "EjbMessageDriven";

}
