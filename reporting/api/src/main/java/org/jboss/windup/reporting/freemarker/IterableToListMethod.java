package org.jboss.windup.reporting.freemarker;

import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.util.ExecutionStatistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns the given Iterable into a List.
 * <p>
 * Example call: iteratorToList(Iterable).
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class IterableToListMethod implements WindupFreeMarkerMethod {
    private static final String NAME = "iterableToList";

    @Override
    public void setContext(GraphRewrite event) {
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        try {
            if (arguments.size() != 1)
                throw new TemplateModelException("Error, method expects one argument (an Iterable)");

            Iterable iterable = FreeMarkerUtil.freemarkerWrapperToIterable(arguments.get(0));
            if (iterable instanceof List)
                return (List) iterable;

            List list = new ArrayList();
            iterable.iterator().forEachRemaining(list::add);
            return list;
        } finally {
            ExecutionStatistics.get().end(NAME);
        }
    }

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Turns the given Iterable into a List.";
    }

}
