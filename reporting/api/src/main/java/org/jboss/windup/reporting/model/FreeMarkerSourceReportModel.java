package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.reporting.model.source.SourceReportModel;

/**
 * This extends SourceReportModel with some functions that are used by our source rendering template.
 */
@TypeValue(FreeMarkerSourceReportModel.TYPE)
public interface FreeMarkerSourceReportModel extends SourceReportModel {
    String TYPE = "FreeMarkerSourceReportModel";

    /**
     * This is used by the Javascript in the source rendering template to provide code assist blocks in the rendered
     * output.
     */
    default String getSourceBlock() {
        StringBuilder builder = new StringBuilder();

        boolean first = true;
        for (InlineHintModel line : getSourceFileModel().getInlineHints()) {
            if (!first) {
                builder.append(",");
            }
            builder.append(line.getLineNumber());

            if (first) {
                first = false;
            }
        }

        return builder.toString();
    }
}
