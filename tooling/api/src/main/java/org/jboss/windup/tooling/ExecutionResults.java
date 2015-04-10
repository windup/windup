package org.jboss.windup.tooling;

import org.jboss.windup.tooling.data.Classification;
import org.jboss.windup.tooling.data.Hint;
import org.jboss.windup.tooling.data.ReportLink;

/**
 * Contains the results of running Windup. This contains all {@link Classification}s, {@link Hint}s, and information about how to find the reports
 * that were produced.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ExecutionResults
{
    /**
     * Contains all {@link Classification}s produced by this run of Windup.
     */
    Iterable<Classification> getClassifications();

    /**
     * Contains all {@link Hint}s produced by this run of Windup.
     */
    Iterable<Hint> getHints();

    /**
     * Contains information about all of the reports produced by Windup and how to find them.
     */
    Iterable<ReportLink> getReportLinks();
}
