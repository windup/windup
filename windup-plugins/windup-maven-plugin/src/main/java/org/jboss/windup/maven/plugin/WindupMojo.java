package org.jboss.windup.maven.plugin;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jboss.windup.WindupEnvironment;
import org.jboss.windup.maven.plugin.util.WindupUtils;
import org.jboss.windup.reporting.ReportEngine;

@Mojo(name = "windup", requiresDependencyResolution = ResolutionScope.COMPILE, aggregator=true)
@Execute(phase=LifecyclePhase.GENERATE_SOURCES)
public class WindupMojo extends AbstractMojo {
	
	@Parameter(defaultValue = "${basedir}")
	private File inputDirectory;

	@Parameter(defaultValue = "${project.reporting.outputDirectory}/windup")
	private File outputDirectory;

	@Parameter(required = true)
	private String[] packages;

	@Parameter
	private String[] excludePackages;
	
	@Parameter
	private String logLevel;

	@Parameter(defaultValue = "true")
	private Boolean source;

	@Parameter
	private Boolean fetchRemote;

	@Parameter(defaultValue = "false")
	private Boolean captureLog;

	@Parameter
	private String targetPlatform;

	private static final char WINDUP_PACKAGE_SEPERATOR = ':';

	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			WindupEnvironment settings = new WindupEnvironment();

			if (targetPlatform != null) {
				settings.setTargetPlatform(targetPlatform);
			}

			if (fetchRemote != null) {
				settings.setFetchRemote(fetchRemote.toString());
			}

			String inPackages = WindupUtils.convertArrayToString(packages,
					WINDUP_PACKAGE_SEPERATOR);

			if (excludePackages != null) {
				String exPackages = WindupUtils.convertArrayToString(
						excludePackages, WINDUP_PACKAGE_SEPERATOR);
				settings.setExcludeSignature(exPackages);
			}

			settings.setCaptureLog(captureLog);
			
			settings.setLogLevel(logLevel);

			settings.setPackageSignature(inPackages);

			settings.setSource(source);

			// Run Windup.
			ReportEngine engine = new ReportEngine(settings);
			engine.process(inputDirectory, outputDirectory);
		} catch (IOException e) {
			getLog().error(e.getMessage(), e);
		}

	}

}
