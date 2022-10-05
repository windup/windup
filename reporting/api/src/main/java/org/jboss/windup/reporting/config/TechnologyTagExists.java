package org.jboss.windup.reporting.config;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderFind;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Returns true if there are {@link TechnologyTag} entries that match the given technology tag name.
 */
public class TechnologyTagExists extends GraphCondition {
    private String namePattern;
    private String filename;

    private TechnologyTagExists(String namePattern) {
        this.namePattern = "[\\s\\S]*" + namePattern + "[\\s\\S]*";
    }

    /**
     * Specifies the regular expression to use when searching {@link TechnologyTagModel} entries.
     */
    public static TechnologyTagExists withName(String namePattern) {
        return new TechnologyTagExists(namePattern);
    }

    /**
     * Only consider entries that reference a file with the given filename.
     */
    public TechnologyTagExists in(String filename) {
        this.filename = filename;
        return this;
    }


    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context) {
        QueryBuilderFind q = Query.fromType(TechnologyTagModel.class);
        if (StringUtils.isNotBlank(filename)) {
            q.piped(new QueryGremlinCriterion() {
                private static final String TECHNOLOGYTAG_STEP = "technologyTagModel";

                @Override
                public void query(GraphRewrite event, GraphTraversal<?, Vertex> pipeline) {
                    pipeline.as(TECHNOLOGYTAG_STEP);
                    pipeline.out(TechnologyTagModel.TECH_TAG_TO_FILE_MODEL);
                    pipeline.has(FileModel.FILE_NAME, filename);
                    pipeline.select(TECHNOLOGYTAG_STEP);
                }
            });
        }
        q.withProperty(TechnologyTagModel.NAME, QueryPropertyComparisonType.REGEX, namePattern);
        return q.evaluate(event, context);
    }

}
