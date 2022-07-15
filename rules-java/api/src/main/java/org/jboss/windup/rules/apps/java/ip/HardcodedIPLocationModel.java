package org.jboss.windup.rules.apps.java.ip;

import org.jboss.windup.reporting.model.InlineHintModel;

import org.jboss.windup.graph.model.TypeValue;

/**
 * Contains the location of a hard-coded IP address within a file
 */
@TypeValue(HardcodedIPLocationModel.TYPE)
public interface HardcodedIPLocationModel extends InlineHintModel {
    String TYPE = "HardcodedIPLocationModel";
}
