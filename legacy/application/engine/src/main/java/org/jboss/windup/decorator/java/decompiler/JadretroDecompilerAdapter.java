/*
 * Copyright (c) 2013 Red Hat, Inc. and/or its affiliates.
 *  
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *      Brad Davis - bradsdavis@gmail.com - Modification to meet execution demands on Mac and Linux.
*/
package org.jboss.windup.decorator.java.decompiler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.windup.util.FatalWindupException;
import org.jboss.windup.util.LogController;

/**
 * Facade for the Jadretro library's main method and the Jad decompiler execution. 
 * 
 * JadRetro is a command-line utility that could helps to successfully decompile Java classes created by the Java compilers of Java 1.4, Java 1.5 or later.
 * JadRetro operates by transforming the specified Java class files (if needed) into ones which could be processed correctly by 
 * an old Java decompiler (designed to work with classes of Java 1.3 or earlier).
 * JadRetro is not a decompiler itself, it is a class transformer helping some old (but good) Java decompilers 
 * to convert more class files and/or generate more correct source code.
 * 
 * http://jadretro.sourceforge.net/
 * 
 * To use this adapter successfully the Jad decompiler must be setup on the environment.
 * It can be downloaded from http://www.varaneckas.com/jad/
 * 
 * The Jad executable/binary will have to be added the PATH environment variable for the environment it is be run. 
 * 
 * @author Jeff Lindesmith (jlindesm@redhat.com)
 * 
 */
public class JadretroDecompilerAdapter implements DecompilerAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(JadretroDecompilerAdapter.class);

	private final String APP_NAME;
	
	public JadretroDecompilerAdapter() {
		LogController.LoggingAdapter.tieSystemOutAndErrToLog();
		

		
		if(SystemUtils.IS_OS_WINDOWS) {
			APP_NAME = "jad.exe";
		}
		else {
			APP_NAME = "jad";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.tattletale.reporting.clz.util.DecompilerAdapter#decompile(java.lang.String, java.lang.String)
	 */
	@Override
	public void decompile(String className, String classLocation, String sourceOutputLocation) {
		LOG.info("Decompiling: " + className);
		net.sf.jadretro.Main.main(new String [] {classLocation}); 
		executeJad(new File(classLocation), new File(sourceOutputLocation));
		LOG.info("... Complete");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.tattletale.reporting.clz.util.DecompilerAdapter#decompile(java.io.File, java.io.File)
	 */
	@Override
	public void decompile(String className, File classLocation, File sourceOutputLocation) {
		LOG.info("Decompiling: " + className + " to: "+sourceOutputLocation.getAbsolutePath());
		net.sf.jadretro.Main.main(new String [] {classLocation.getAbsolutePath()}); 
		executeJad(classLocation,sourceOutputLocation);
		LOG.info("... Complete");
	}
	
	private void executeJad(File classLocation, File sourceOutputLocation) {
		
		try {
			// Build command array
			CommandLine cmdLine = new CommandLine(APP_NAME);
			cmdLine.addArgument("-d");
			cmdLine.addArgument("${outputLocation}");
			cmdLine.addArgument("-f");
			cmdLine.addArgument("-o");
			cmdLine.addArgument("-s");
			cmdLine.addArgument("java");
			cmdLine.addArgument("${classLocation}");
			
			Map<String, Object> argMap = new HashMap<String, Object>();
			argMap.put("outputLocation", sourceOutputLocation);
			argMap.put("classLocation", classLocation);
			cmdLine.setSubstitutionMap(argMap);
			
			DefaultExecutor executor = new DefaultExecutor();
			executor.setExitValue(0);
			ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
			executor.setWatchdog(watchdog);
			int exitValue = executor.execute(cmdLine);
			
			LOG.debug("Decompiler exited with exit code: "+exitValue);
			
        	if(!sourceOutputLocation.exists()) {
    			LOG.error("Expected decompiled source: "+sourceOutputLocation.getAbsolutePath()+"; did not find file.  This likey means that the decompiler did not successfully decompile the class.");
    		}
        	else {
        		LOG.debug("Decompiled to: "+sourceOutputLocation.getAbsolutePath());
        	}
        	
        } catch (IOException e) {
            throw new FatalWindupException("Error running "+APP_NAME+" decompiler.  Validate that "+APP_NAME+" is on your PATH.", e);
        }
	}
}
