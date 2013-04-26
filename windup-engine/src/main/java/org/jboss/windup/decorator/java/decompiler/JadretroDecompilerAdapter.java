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
package org.jboss.windup.decorator.java.decompiler;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	private static final Log LOG = LogFactory.getLog(JadretroDecompilerAdapter.class);

	public JadretroDecompilerAdapter() {
		LogController.LoggingAdapter.tieSystemOutAndErrToLog();
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
		executeJad(classLocation,sourceOutputLocation);
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
		executeJad(classLocation.getAbsolutePath(),sourceOutputLocation.getAbsolutePath());
		LOG.info("... Complete");
	}
	
	private void executeJad(String classLocation, String sourceOutputLocation) {
		
		String appName = "jad";
		if(SystemUtils.IS_OS_WINDOWS) {
			appName = "jad.exe";
		}
		
		Process process = null;
		try {
			// Build command array
			String[] cmdArray = new String[] {appName,"-d",sourceOutputLocation,"-f","-o","-s","java",classLocation};
            process = Runtime.getRuntime().exec(cmdArray);
            
        	File sol = new File(sourceOutputLocation);
        	
        	//if the file does not exist, try for 2.5 seconds to wait for it..
        	int cancelAfterFive = 0;
        	while(!sol.exists() && cancelAfterFive < 5) {
        		
        		Thread.sleep(500);
        		cancelAfterFive++;
        	}

        	if(!sol.exists()) {
    			LOG.error("Expected decompiled source: "+sol.getAbsolutePath()+"; did not find file.  This likey means that the decompiler did not successfully decompile the class.");
    		}
        	LOG.debug("Decompiled to: "+sol.getAbsolutePath());
        } catch (IOException e) {
            LOG.error("Error running jad decompiler: " + e);
        } catch (InterruptedException e) {
        	LOG.error("Error running jad decompiler: " + e);
		}		
	}
}
