package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.BeanModel;

import freemarker.template.TemplateModelException;
import java.util.ArrayList;

/**
 * Turns the given Iterable into a List.
 *
 * Example call: iteratorToList(Iterable).
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class IterableToListMethod implements WindupFreeMarkerMethod
{
    private static final String NAME = "iterableToList";

    @Override
    public void setContext(GraphRewrite event)
    {
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
            throw new TemplateModelException("Error, method expects one argument (an Iterable)");

        BeanModel iterableModelArg = (BeanModel) arguments.get(0);
        Iterable iterable = (Iterable) iterableModelArg.getWrappedObject();
        List list = new ArrayList();
        iterable.iterator().forEachRemaining(list::add);
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
        return "Turns the given Iterable into a List.";
    }

}
