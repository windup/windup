package org.jboss.windup.reporting.rules.generation.techreport;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.service.TagGraphService;

import java.util.List;

/**
 * Returns the TagModel of the given name, or null if it doesn't exist.
 * <p>
 * Call like this:  getTagModel("tag-name")
 */
public class GetTagModelByNameMethod implements WindupFreeMarkerMethod {
    private GraphContext graphContext;

    @Override
    public String getMethodName() {
        return "getTagModelByName";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        if (arguments.size() != 1)
            throw new TemplateModelException("Expected one String argument - the name of the tag.");

        SimpleScalar stringModel = (SimpleScalar) arguments.get(0);
        String tagName = stringModel.getAsString();

        return new TagGraphService(graphContext).getTagByName(tagName);
    }


    @Override
    public String getDescription() {
        return "Returns the TagModel of the given name, or null if it doesn't exist.";
    }

    @Override
    public void setContext(GraphRewrite event) {
        this.graphContext = event.getGraphContext();
    }

}
