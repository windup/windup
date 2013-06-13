package org.jboss.windup.reporting;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.util.ClasspathUtils;
import org.jboss.windup.resource.type.archive.ArchiveMeta;

public class TattletaleReporter implements Reporter {

	private static final Log LOG = LogFactory.getLog(TattletaleReporter.class);
	private final String APP_NAME;

	// "java -Xmx512m -jar tattletale.jar myApp.jar#yourApp.war#amazingApp.ear report"

	public TattletaleReporter() {
		if (SystemUtils.IS_OS_WINDOWS) {
			APP_NAME = "java.exe";
		} else {
			APP_NAME = "java";
		}
	}

	protected File getReferenceToTattletaleJar() {
		return getReferenceToJar(org.jboss.tattletale.Main.class);
	}
	
	protected File getReferenceToJavassist() {
		return getReferenceToJar(javassist.compiler.Javac.class);
	}
	
	protected File getReferenceToJar(Class classToSearch) {
		String className = classToSearch.getCanonicalName();
		className = StringUtils.replace(className, ".", "/");
		className = className+".class";
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String path = (classLoader.getResource(className).getPath());
		
		path = StringUtils.removeStart(path, "file:");
		path = StringUtils.substringBefore(path, "!");
		
		return new File(path);
	}
	
	
	
	
	
	@Override
	public void process(ArchiveMeta archive, File reportDirectory) {
		try {
			System.out.println("ARCHIVE:" + archive.getRelativePath());

			// Build command array
			CommandLine cmdLine = new CommandLine(APP_NAME);
			cmdLine.addArgument("-Xmx512m");
			cmdLine.addArgument("-cp");
			cmdLine.addArgument("${classPath}");
			cmdLine.addArgument("org.jboss.tattletale.Main");
			
			cmdLine.addArgument("${inputArchive}");
			cmdLine.addArgument("${outputLocation}");
			
			File javassistPath = getReferenceToJavassist();
			File tattletalePath = getReferenceToTattletaleJar();
			
			String cpParam = javassistPath+File.pathSeparator+tattletalePath;
			
			File outputPath = new File(reportDirectory.getAbsolutePath()
					+ File.separator + "tattletale");

			Map<String, Object> argMap = new HashMap<String, Object>();
			argMap.put("classPath", cpParam);
			argMap.put("inputArchive", archive.getFilePointer());
			argMap.put("outputLocation", outputPath);
			cmdLine.setSubstitutionMap(argMap);

			LOG.info("Running Tattletale: " + cmdLine.toString());

			DefaultExecutor executor = new DefaultExecutor();
			executor.setExitValue(0);
			ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
			executor.setWatchdog(watchdog);
			int exitValue = executor.execute(cmdLine);

			LOG.debug("Decompiler exited with exit code: " + exitValue);

			if (!outputPath.exists()) {
				//LOG.error("Expected decompiled source: " + outputPath.getAbsolutePath() + "; did not find file.  This likey means that the decompiler did not successfully decompile the class.");
			} else {
				//LOG.debug("Tattletale Created: " + outputPath.getAbsolutePath());
			}
		} catch (Exception e) {
			LOG.error("Exception running Tattletale.", e);
		}
	}

}
