package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.MessageDrivenDao;
import org.jboss.windup.rules.apps.ejb.model.MessageDrivenBeanFacetModel;

@Singleton
public class MessageDrivenDaoImpl extends BaseDaoImpl<MessageDrivenBeanFacetModel> implements MessageDrivenDao {
	public MessageDrivenDaoImpl() {
		super(MessageDrivenBeanFacetModel.class);
	}
}
