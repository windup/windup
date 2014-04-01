package org.jboss.windup.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jboss.windup.WindupEnvironment;
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

	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			WindupEnvironment settings = new WindupEnvironment();

			if (targetPlatform != null) {
				settings.setTargetPlatform(targetPlatform);
			}

			if (fetchRemote != null) {
				settings.setFetchRemote(fetchRemote);
			}

			if (excludePackages != null) {
				settings.setExcludeJavaPackageSignature(Arrays.asList(excludePackages));
			}

			settings.setCaptureLog(captureLog);
			
			settings.setLogLevel(logLevel);

			settings.setIncludeJavaPackageSignature(Arrays.asList(packages));

			settings.setSource(source);
			
			settings.setInputPath(inputDirectory);
			settings.setOutputPath(outputDirectory);

			// Run Windup.
			ReportEngine engine = new ReportEngine(settings);
			engine.process();
		} catch (IOException e) {
			getLog().error(e.getMessage(), e);
		}

	}

}
