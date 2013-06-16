/*
 * Copyright (c) 2013 Red Hat, Inc. and/or its affiliates.
 *  
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *      Brad Davis - bradsdavis@gmail.com - Initial API and implementation
*/
package org.jboss.windup.reporting.transformers;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Resolves a given Meta file to it's proper tranformer.
 * 
 * @author bradsdavis
 * 
 */
public class MetaResultTransformResolver implements InitializingBean {

	private static final Log LOG = LogFactory.getLog(MetaResultTransformResolver.class);

	private Map<String, MetaResultTransformer> transformResolver;
	private FileMetaTransformer defaultTransformer;
	private List<String> resolvers;

	public void setDefaultTransformer(FileMetaTransformer defaultTransformer) {
		this.defaultTransformer = defaultTransformer;
	}

	public MetaResultTransformer<?> resolveTransformer(String className) {
		if (transformResolver.containsKey(className)) {
			return transformResolver.get(className);
		}

		return defaultTransformer;
	}

	public MetaResultTransformer<?> resolveTransformer(Class className) {
		return this.resolveTransformer(className.toString());
	}

	public void setResolvers(List<String> resolvers) {
		this.resolvers = resolvers;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		transformResolver = new HashMap<String, MetaResultTransformer>(resolvers.size());
		for (String clz : resolvers) {
			instantiateMapper(clz);
		}
	}

	private void instantiateMapper(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class clz = Class.forName(className);
		MetaResultTransformer transformer = (MetaResultTransformer) clz.newInstance();
		String key = (((ParameterizedType) clz.getGenericSuperclass()).getActualTypeArguments()[0]).toString();
		transformResolver.put(key, transformer);
	}
}
