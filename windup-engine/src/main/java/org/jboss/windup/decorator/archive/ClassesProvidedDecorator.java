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
package org.jboss.windup.decorator.archive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javassist.ClassPool;
import javassist.CtClass;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.MetaDecorator;
import org.jboss.windup.graph.clz.ApplicationClz;
import org.jboss.windup.graph.clz.GraphableClz;
import org.jboss.windup.graph.clz.ProfileClz;
import org.jboss.windup.graph.clz.UnknownClz;
import org.jboss.windup.graph.profile.Profile;
import org.jboss.windup.metadata.decoration.ClassGraph;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.metadata.type.archive.ZipMetadata;
import org.jboss.windup.util.CustomerPackageResolver;
import org.jboss.windup.util.ProfileResolver;


public class ClassesProvidedDecorator implements MetaDecorator<ZipMetadata> {
	public static String MISSING_DEPENDENCIES_TO_APPLICATION_CLASSES = "MISSING_DEPENDENCIES_TO_APPLICATION_CLASSES";
	public static String PROVIDED_CLASS_LOCATIONS = "PROVIDED_CLASS_LOCATIONS";
	
	
	private static final Log LOG = LogFactory.getLog(ClassesProvidedDecorator.class);
	
	private CustomerPackageResolver customerPackageResolver;
	private ProfileResolver profileResolver;
	
	public void setProfileResolver(ProfileResolver profileResolver) {
		this.profileResolver = profileResolver;
	} 
	
	public void setCustomerPackageResolver(
			CustomerPackageResolver customerPackageResolver) {
		this.customerPackageResolver = customerPackageResolver;
	}
	
	@Override
	public void processMeta(ZipMetadata meta) {
		if(true) return;
		//recurse... only start at the top.
		if(meta.getArchiveMeta() != null) {
			return;
		}
		
		Map<String, GraphableClz> classGraph = new HashMap<String, GraphableClz>();
		recursivelyCollectRequiredProvided(meta, classGraph);
		
		//now, add in the ones that are provided by platforms.
		for(Profile profile : profileResolver.getProfiles()) {
			for(ProfileClz qual : profile.getProvided()) {
				if(!classGraph.containsKey(qual.getClassName())) {
					classGraph.put(qual.getClassName(), qual);
				}
			}
		}
		
		Set<String> required = new HashSet<String>();
		for(GraphableClz t : classGraph.values()) {
			if(t instanceof ApplicationClz) {
				ApplicationClz cg = (ApplicationClz)t;
				required.addAll(cg.getUnresolvedDependencies());
			}
		}
		
		Set<String> delta = new TreeSet<String>(required);
		delta.removeAll(classGraph.keySet());
		
		
		for(String missing : delta) {
			classGraph.put(missing, new UnknownClz(missing));
		}
		
		//now, we simply need to resolve.
		for(GraphableClz g : classGraph.values()) {
			if(g instanceof ApplicationClz) {
				for(String dep : ((ApplicationClz) g).getUnresolvedDependencies()) {
					GraphableClz depResolved = classGraph.get(dep);
					depResolved.getProvidesFor().add(g);
					((ApplicationClz) g).getDependsOn().add(depResolved);
				}
			}
		}

		
		Map<String, Set<String>> missingToAffected = new TreeMap<String, Set<String>>();
		for(GraphableClz g : classGraph.values()) {
			//now, print it out!
			if(g instanceof ApplicationClz) {
				if(customerPackageResolver.isCustomerPkg(g.getClassName())) {
					findProblem(g, g, 0, new HashSet<GraphableClz>(), missingToAffected);
				}
			}
		}
		
		ClassGraph decoration = new ClassGraph();
		decoration.setClassGraph(classGraph);
		meta.getDecorations().add(decoration);
		
		//now, add the problems to the context.
		meta.getContext().put(MISSING_DEPENDENCIES_TO_APPLICATION_CLASSES, missingToAffected);
	}
	
	protected void findProblem(GraphableClz source, GraphableClz g, Integer i, Set<GraphableClz> transversed, Map<String, Set<String>> missingToAffected) {
		if(!transversed.contains(g)) {
			transversed.add(g);
			
			if(g instanceof ApplicationClz) { 
				for(GraphableClz p : ((ApplicationClz) g).getDependsOn()) {
					findProblem(source, p, new Integer(i+1), transversed, missingToAffected);
				}
			}
			else if(g instanceof UnknownClz)
			{
				if(!missingToAffected.containsKey(g.getClassName())) {
					missingToAffected.put(g.getClassName(), new TreeSet<String>());
				}
				missingToAffected.get(g.getClassName()).add(source.getClassName());
			}
			
		}
	}
	
	protected String extractClassName(String entryName) {
		String className = StringUtils.replace(entryName, "\\", "/");
		className = StringUtils.removeStart(className, "/");
		className = StringUtils.replace(className, "/", ".");
		className = StringUtils.removeEnd(className, ".class");
		className = StringUtils.removeEnd(className, ".java");

		// account for WAR classes.
		if (StringUtils.contains(className, "WEB-INF.classes.")) {
			className = StringUtils.substringAfter(className, "WEB-INF.classes.");
		}
		return className;
	}
	
	protected void recursivelyCollectRequiredProvided(ZipMetadata meta, Map<String, GraphableClz> provided) {
		try {
			ZipEntry entry;
			Enumeration<?> e = meta.getZipFile().entries();
			// locate a random class entry...
			while (e.hasMoreElements()) {
				entry = (ZipEntry) e.nextElement();

				if (StringUtils.endsWith(entry.getName(), ".class")) {
					String className = extractClassName(entry.getName());
					
					Set<String> required = extractImports(meta.getZipFile(), entry);
					ApplicationClz cg = new ApplicationClz(meta, className, required);
					provided.put(className, cg);
				}
			}
		}
		catch (Exception e) {
			LOG.error("Exception getting JDK version.", e);
		}
		
		for(ArchiveMetadata child : meta.getNestedArchives()) {
			ZipMetadata cast = (ZipMetadata) child;
			this.recursivelyCollectRequiredProvided(cast, provided);
		}
	}
	
	
	protected Set<String> extractImports(ZipFile zipFile, ZipEntry entry) {
		try {
			// otherwise, load the class and get its imports.

			CtClass ctClz = new ClassPool().makeClass(zipFile.getInputStream(entry));
			if (LOG.isDebugEnabled()) {
				for (String clz : ctClz.getClassFile2().getInterfaces()) {
					LOG.debug("Interfaces: " + clz);
				}
				LOG.debug("Super Class: " + ctClz.getClassFile2().getSuperclass());
			}
			Set<String> clzImports = new HashSet<String>(castList(String.class, ctClz.getRefClasses()));
			return clzImports;
		}
		catch (Exception e) {
			LOG.error(e);
			return null;
		}
	}
	
	public static <T> List<T> castList(Class<? extends T> aclass, Collection<?> c) {
		List<T> r = new ArrayList<T>(c.size());
		for (Object o : c) {
			r.add(aclass.cast(o));
		}
		return r;
	}
	
	

}
