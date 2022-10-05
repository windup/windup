package org.jboss.windup.reporting.rules.generation.techreport;

import freemarker.ext.beans.BooleanModel;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

import java.util.List;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.model.TagModel;
import org.jboss.windup.reporting.service.TagGraphService;
import org.jboss.windup.util.ExecutionStatistics;

/**
 * Returns true if the given tag #1 is under the other tag #2.
 *
 * <p> Called from a freemarker template as follows:
 *
 * <pre>isTagUnderTag(tag1: TagModel, tag2: TagMode): boolean
 * </pre>
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class IsTagUnderTagMethod implements WindupFreeMarkerMethod {
    public static final Logger LOG = Logger.getLogger(IsTagUnderTagMethod.class.getName());
    private static final String NAME = "isTagUnderTag";

    private GraphContext graphContext;

    @Override
    public void setContext(GraphRewrite event) {
        this.graphContext = event.getGraphContext();
    }

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);

        // Function arguments
        if (arguments.size() < 2) {
            throw new TemplateModelException("Expected 2 or 3 arguments - a subsector tag, a row tag and optionally, a project.");
        }

        StringModel tag1Arg = (StringModel) arguments.get(0);
        TagModel subTag = (TagModel) tag1Arg.getWrappedObject();

        StringModel tag2Arg = (StringModel) arguments.get(1);
        TagModel maybeSuperTag = (TagModel) tag2Arg.getWrappedObject();

        // The project. May be null -> count from all applications.
        boolean countIfSame = false;
        if (arguments.size() >= 3) {
            BooleanModel countRootArg = (BooleanModel) arguments.get(2);
            if (null != countRootArg)
                countIfSame = countRootArg.getAsBoolean();
        }

        boolean isUnder = new TagGraphService(this.graphContext).isTagUnderTag(subTag, maybeSuperTag, countIfSame);

        ExecutionStatistics.get().end(NAME);
        return isUnder;
    }
}
