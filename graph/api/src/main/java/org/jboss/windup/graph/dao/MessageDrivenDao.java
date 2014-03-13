package org.jboss.windup.graph.dao;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacet;

@Singleton
public class MessageDrivenDao extends BaseDao<MessageDrivenBeanFacet> {
	public MessageDrivenDao() {
		super(MessageDrivenBeanFacet.class);
	}
}
