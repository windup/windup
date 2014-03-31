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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AsyncDecompilerAdapter implements DecompilerAdapter {
	private static final Log LOG = LogFactory.getLog(AsyncDecompilerAdapter.class);

	private ExecutorService executor = Executors.newCachedThreadPool();
	private Integer timeoutSeconds;
	private DecompilerAdapter decompilerAdapter;

	public void setTimeoutSeconds(Integer timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}

	public void setDecompilerAdapter(DecompilerAdapter decompilerAdapter) {
		this.decompilerAdapter = decompilerAdapter;
	}

	@Override
	public void decompile(String className, String classLocation, String sourceOutputLocation) {
		decompile(className, new File(classLocation), new File(sourceOutputLocation));
	}

	@Override
	public void decompile(String className, File classLocation, File sourceOutputLocation) {
		DecompilerTask dt = new DecompilerTask(className, classLocation, sourceOutputLocation, decompilerAdapter);
		Future<?> future = executor.submit(dt);
		try {
			Object result = future.get(timeoutSeconds, TimeUnit.SECONDS);
		}
		catch (TimeoutException ex) {
			LOG.error("Timed out execution of : " + dt.toString());
		}
		catch (InterruptedException e) {
			LOG.error("Interrupted out execution of : " + dt.toString());
		}
		catch (ExecutionException e) {
			LOG.error("Execution execution of : " + dt.toString());
		}
		finally {
			future.cancel(true); // may or may not desire this
		}
	}

	public class DecompilerTask implements Runnable {
		private DecompilerAdapter decompilerAdapter;
		private String className;
		private File classLocation;
		private File sourceOutputLocation;

		public DecompilerTask(String className, File classLocation, File sourceOutputLocation, DecompilerAdapter decompilerAdapter) {
			this.classLocation = classLocation;
			this.sourceOutputLocation = sourceOutputLocation;
			this.className = className;
			this.decompilerAdapter = decompilerAdapter;
		}

		@Override
		public void run() {
			decompilerAdapter.decompile(className, classLocation, sourceOutputLocation);
		}

		@Override
		public String toString() {
			return "DecompilerTask [decompilerAdapter=" + decompilerAdapter.getClass() + ", className=" + className + ", classLocation=" + classLocation
					+ ", sourceOutputLocation=" + sourceOutputLocation + "]";
		}
	}
}
