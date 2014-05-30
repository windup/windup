package org.jboss.windup;

import org.jboss.windup.WindupEngine;
import org.jboss.windup.WindupEnvironment;
import org.jboss.windup.reporting.ReportEngine;

/**
 * <p>
 * Factory service for creating various Windup engines.
 * </p>
 */
public class WindupFactory {
	/**
	 * @param settings
	 *            Windup settings to create the {@link WindupEngine} with
	 * 
	 * @return {@link WindupEngine} created with the given
	 *         {@link WindupEnvironment}
	 */
	public WindupEngine createWindupEngine(WindupEnvironment settings) {
		return new WindupEngine(settings);
	}
	
	/**
	 * @param settings
	 *            Windup settings to create the {@link ReportEngine} with
	 * @param engine
	 *            Windup engine to create the {@link ReportEngine} with
	 * 
	 * @return Windup {@link ReportEngine} created with the given Windup
	 *         settings and Windup engine
	 */
	public ReportEngine createReportEngine(WindupEnvironment settings, WindupEngine engine) {
		return new ReportEngine(settings, engine);
	}
}