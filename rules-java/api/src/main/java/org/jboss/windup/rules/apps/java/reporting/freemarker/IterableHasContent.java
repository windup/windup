package org.jboss.windup.rules.apps.java.reporting.freemarker;

import freemarker.core.CollectionAndSequence;
import freemarker.ext.beans.BeanModel;
import freemarker.template.DefaultIterableAdapter;
import freemarker.template.DefaultListAdapter;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModelException;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;

import java.util.List;

/**
 * Returns whether an Iterable has values within it.
 * <p>
 * Called as follows:
 * <p>
 * getPrettyPathForFile(Iterable)
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class IterableHasContent implements WindupFreeMarkerMethod {
    private static final String NAME = "iterableHasContent";

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        try {
            if (arguments.size() != 1) {
                throw new TemplateModelException("Error, method expects one argument (Iterable)");
            }
            return hasContent(arguments.get(0));
        } finally {
            ExecutionStatistics.get().end(NAME);
        }
    }

    private boolean hasContent(Object arg) throws TemplateModelException {
        if (arg instanceof BeanModel) {
            BeanModel beanModel = (BeanModel) arg;
            return ((Iterable) beanModel.getWrappedObject()).iterator().hasNext();
        } else if (arg instanceof SimpleSequence) {
            SimpleSequence simpleSequence = (SimpleSequence) arg;
            return simpleSequence.size() > 0;
        } else if (arg instanceof DefaultIterableAdapter) {
            DefaultIterableAdapter adapter = (DefaultIterableAdapter) arg;
            return adapter.iterator().hasNext();
        } else if (arg instanceof DefaultListAdapter) {
            DefaultListAdapter adapter = (DefaultListAdapter) arg;
            return adapter.size() > 0;
        } else if (arg instanceof DefaultIterableAdapter) {
            DefaultIterableAdapter adapter = (DefaultIterableAdapter) arg;
            return adapter.iterator().hasNext();
        } else if (arg instanceof CollectionAndSequence) {
            CollectionAndSequence sequence = (CollectionAndSequence) arg;
            return sequence.size() > 0;
        } else {
            throw new WindupException("Unrecognized type passed to " + getMethodName() + "(): "
                    + arg.getClass().getCanonicalName());
        }
    }

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes an Iterable as a parameter and checks to see whether items exist in the Iterable.";
    }
}
