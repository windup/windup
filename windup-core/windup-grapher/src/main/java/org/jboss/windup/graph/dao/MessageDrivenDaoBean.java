package org.jboss.windup.graph.dao;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacet;

@Singleton
public class MessageDrivenDaoBean extends BaseDaoBean<MessageDrivenBeanFacet> {
	public MessageDrivenDaoBean() {
		super(MessageDrivenBeanFacet.class);
	}
}
