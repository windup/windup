package org.jboss.windup.rules.apps.java.ip;

import org.jboss.windup.reporting.model.ApplicationReportModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains a Java package name
 *
 */
@TypeValue(StaticIPLocationReportModel.TYPE)
public interface StaticIPLocationReportModel extends ApplicationReportModel
{
	public static final String TYPE = "StaticIPLocationReport";
	public static final String CONTAINS_STATIC_IP_FILES = "staticIpFiles";
	
	@Adjacency(label = CONTAINS_STATIC_IP_FILES, direction = Direction.OUT)
	void addStaticIPLocation(StaticIPLocationModel location);
	
    /**
     * Get the files that were ignored.
     */
    @Adjacency(label = CONTAINS_STATIC_IP_FILES, direction = Direction.OUT)
    public Iterable<StaticIPLocationModel> getStaticIPLocations();

}
