package org.jboss.windup.rules.apps.java.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;

import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModelException;

/**
 * Returns whether an Iterable has values within it.
 * 
 * Called as follows:
 * 
 * getPrettyPathForFile(fileModel)
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class IterableHasContent implements WindupFreeMarkerMethod
{
    private static final String NAME = "iterableHasContent";

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        try
        {
            if (arguments.size() != 1)
            {
                throw new TemplateModelException("Error, method expects one argument (FileModel)");
            }
            Iterable iterable = getList(arguments.get(0));
            return iterable.iterator().hasNext();
        }
        finally
        {
            ExecutionStatistics.get().end(NAME);
        }
    }

    @SuppressWarnings("unchecked")
    private Iterable getList(Object arg) throws TemplateModelException
    {
        if (arg instanceof BeanModel)
        {
            BeanModel beanModel = (BeanModel) arg;
            return (Iterable) beanModel.getWrappedObject();
        }
        else if (arg instanceof SimpleSequence)
        {
            SimpleSequence simpleSequence = (SimpleSequence) arg;
            return simpleSequence.toList();
        }
        else
        {
            throw new WindupException("Unrecognized type passed to: " + getMethodName() + ": "
                        + arg.getClass().getCanonicalName());
        }
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes an Iterable as a parameter and checks to see whether items exist in the Iterable.";
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        // no-op
    }

}
