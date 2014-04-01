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
package org.jboss.windup.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class LogController {
	private static final Log LOG = LogFactory.getLog(LogController.class);

	public static void setLogLevel(Level level) {
		Logger root = Logger.getLogger("org.jboss.windup");
		root.setLevel(level);
		LOG.info("Set logger to: " + level);
	}

	public static void setLogLevel(String level) {
		Level l = Level.toLevel(level, Level.INFO);
		LogController.setLogLevel(l);
	}

	public static void addFileAppender(File outputLocation) {
		String hashCode = outputLocation.toString().hashCode() + "";

		LOG.debug("Adding logger: " + outputLocation.getAbsolutePath() + " : " + hashCode);
		Logger root = Logger.getRootLogger();
		PatternLayout pl = new PatternLayout("%d %-5p [%c] (%t) %m%n");
		try {
			FileAppender fa = new FileAppender(pl, outputLocation.getAbsolutePath());
			fa.setName(hashCode);
			root.addAppender(fa);
			root.setAdditivity(false);
		}
		catch (IOException e) {
			LOG.error("Exception with file appender.", e);
		}
	}

	public static void removeFileAppender(File outputLocation) {
		String hashCode = outputLocation.toString().hashCode() + "";

		LOG.debug("Removing logger: " + outputLocation.getAbsolutePath() + " : " + hashCode);
		Logger root = Logger.getRootLogger();
		root.removeAppender(hashCode);
	}

	public static class LoggingAdapter {
		public static void tieSystemOutAndErrToLog() {
			System.setOut(createLoggingProxy(System.out));
		}

		public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
			return new LogPrintStream(realPrintStream);
		}
	}

	private static class LogPrintStream extends PrintStream {
		private static final Log LOG = LogFactory.getLog(LogController.LogPrintStream.class);

		public LogPrintStream(PrintStream stream) {
			super(stream);
		}

		@Override
		public void println(final String top) {
			LOG.debug(top);
		}

		public void print(final String top) {
			LOG.debug(top);
		}
	}
}
