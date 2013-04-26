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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.util.LogController;

/**
 * Facade for the Fernflower library's main method.
 * 
 * @author Brad Davis (bdavis@redhat.com)
 * 
 */
public class FernflowerDecompilerAdapter implements DecompilerAdapter {
	private static final Log LOG = LogFactory.getLog(FernflowerDecompilerAdapter.class);

	public FernflowerDecompilerAdapter() {
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
	//	ConsoleDecompiler.main(new String[] { classLocation, sourceOutputLocation });
		LOG.info("... Complete");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.tattletale.reporting.clz.util.DecompilerAdapter#decompile(java.io.File, java.io.File)
	 */
	@Override
	public void decompile(String className, File classLocation, File sourceOutputLocation) {
	//	ConsoleDecompiler.main(new String[] { classLocation.getAbsolutePath(), sourceOutputLocation.getAbsolutePath() });
	}
}
