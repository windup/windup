package org.jboss.windup.reporting.config;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderFind;
import org.jboss.windup.graph.model.resource.FileModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Queries the graph looking for {@link FileModel}s.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class FileExists extends GraphCondition
{
    private String filename;

    private FileExists(String filename)
    {
        this.filename = filename;
    }

    public static FileExists withFileName(String filename) {
        return new FileExists(filename);
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        QueryBuilderFind q = Query.fromType(FileModel.class);
        q.withProperty(FileModel.FILE_NAME,filename);
        return q.evaluate(event, context);
    }

}
