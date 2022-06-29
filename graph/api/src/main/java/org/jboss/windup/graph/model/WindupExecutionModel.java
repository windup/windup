package org.jboss.windup.graph.model;

import org.jboss.windup.graph.Property;

/**
 * Information about the Windup execution.
 * Some of the information are stored in memory, see e.g. {@link RuleExecutionResultsListener}.
 */
@TypeValue(WindupExecutionModel.TYPE)
public interface WindupExecutionModel extends WindupVertexFrame {
    String TYPE = "WindupExecutionModel";

    String STOP_MESSAGE = "stopMessage";

    /**
     * A message about where Windup stopped on request.
     */
    @Property(STOP_MESSAGE)
    String getStopMessage();

    /**
     * A message about where Windup stopped on request.
     */
    @Property(STOP_MESSAGE)
    void setStopMessage(String message);
}
