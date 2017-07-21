package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.template.DefaultListAdapter;

import freemarker.template.TemplateModelException;
import java.util.Collections;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.rules.CreateApplicationListReportRuleProvider;

/**
 * Sorts the given list of ApplicationReportModel's by it's root filename or, for VIRTUAL apps, by the name.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class SortApplicationsListMethod implements WindupFreeMarkerMethod
{
    private static final String NAME = "sortApplicationsList";
    private GraphContext context;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
            throw new TemplateModelException("Error, method expects one argument (an Iterable)");

        DefaultListAdapter listModelArg = (DefaultListAdapter) arguments.get(0);
        List<ApplicationReportModel> list = (List<ApplicationReportModel>) listModelArg.getWrappedObject();
        Collections.sort(list, new CreateApplicationListReportRuleProvider.AppRootFileNameComparator());

        ExecutionStatistics.get().end(NAME);
        return list;
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Sorts the given list of ApplicationReportModel's by it's root filename or, for VIRTUAL apps, by the name.";
    }

}
