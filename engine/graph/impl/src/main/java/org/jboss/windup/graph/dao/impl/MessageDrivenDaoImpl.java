package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.MessageDrivenDao;
import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacet;

@Singleton
public class MessageDrivenDaoImpl extends BaseDaoImpl<MessageDrivenBeanFacet> implements MessageDrivenDao {
	public MessageDrivenDaoImpl() {
		super(MessageDrivenBeanFacet.class);
	}
}
