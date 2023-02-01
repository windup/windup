package org.jboss.windup.reporting.freemarker.problemsummary;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gets the issue category IDs (mandatory, potential, etc.) mapped to their names (Migration mandatory, Migration potential, etc)
 */
public class GetIssueCategoriesMethod implements WindupFreeMarkerMethod {
    @Override
    public String getMethodName() {
        return WindupFreeMarkerMethod.super.getMethodName();
    }

    @Override
    public String getDescription() {
        return "Gets the issue category IDs (mandatory, potential, etc.) mapped to their names (Migration mandatory, Migration potential, etc)";
    }

    @Override
    public void setContext(GraphRewrite event) {
        WindupFreeMarkerMethod.super.setContext(event);
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.isEmpty())
            throw new TemplateModelException("Method " + getMethodName() + " requires the following parameters: (GraphRewrite event)");

        final GraphRewrite event = (GraphRewrite) ((StringModel) arguments.get(0)).getWrappedObject();

        GraphService<IssueCategoryModel> issueCategoryModelService = new GraphService<>(event.getGraphContext(), IssueCategoryModel.class);

        return issueCategoryModelService.findAll().stream().collect(Collectors.toMap(IssueCategoryModel::getName, IssueCategoryModel::getCategoryID));
    }
}
