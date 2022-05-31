package org.jboss.windup.reporting.freemarker;

import freemarker.ext.beans.BeanModel;
import freemarker.template.DefaultIterableAdapter;
import freemarker.template.DefaultListAdapter;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.comparator.FilePathComparator;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Sorts file paths according to their alphabetical order.
 * <p>
 * For example:
 * <ul>
 * <li>/foo/bar/baz.class</li>
 * <li>/foo/car/caz.class</li>
 * <li>/foo/hat.class</li>
 * </ul>
 * <p>
 * Would become:
 * <ul>
 * <li>/foo/hat.class</li>
 * <li>/foo/bar/baz.class</li>
 * <li>/foo/car/caz.class</li>
 * </ul>
 * <p>
 * Can be called as follows: sortFilesByPathAscending(Iterable<FileModel>) or sortFilesByPathAscending(Iterable<String>)
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class SortFilesByPathMethod implements WindupFreeMarkerMethod {
    private static final String NAME = "sortFilesByPathAscending";

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes an Iterable<" + FileModel.class.getSimpleName() + "> or Iterable<String> and returns them, ordered alphabetically.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1) {
            throw new TemplateModelException("Error, method expects one argument (Iterable<FileModel>)");
        }
        Iterable<Object> pathIterable = getIterable(arguments.get(0));

        Comparator<Object> fileModelComparator = new Comparator<Object>() {
            final FilePathComparator filePathComparator = new FilePathComparator();

            @Override
            public int compare(Object o1, Object o2) {
                return filePathComparator.compare(getFilePath(o1), getFilePath(o2));
            }

            private String getFilePath(Object o) {
                if (o == null)
                    return null;
                else if (o instanceof FileModel)
                    return ((FileModel) o).getFilePath();
                else if (o instanceof String)
                    return (String) o;
                else
                    throw new IllegalArgumentException("Unrecognized type: " + o.getClass().getName());
            }
        };

        SortedSet<Object> resultSet = new TreeSet<>(fileModelComparator);
        for (Object fm : pathIterable) {
            resultSet.add(fm);
        }

        ExecutionStatistics.get().end(NAME);
        return resultSet;
    }

    @SuppressWarnings("unchecked")
    private Iterable<Object> getIterable(Object arg) throws TemplateModelException {
        if (arg instanceof BeanModel) {
            BeanModel beanModel = (BeanModel) arg;
            return (Iterable<Object>) beanModel.getWrappedObject();
        } else if (arg instanceof SimpleSequence) {
            SimpleSequence simpleSequence = (SimpleSequence) arg;
            return (Iterable<Object>) simpleSequence.toList();
        } else if (arg instanceof DefaultIterableAdapter) {
            DefaultIterableAdapter adapter = (DefaultIterableAdapter) arg;
            return (Iterable<Object>) adapter.getAdaptedObject(Iterable.class);
        } else if (arg instanceof DefaultListAdapter) {
            DefaultListAdapter defaultListAdapter = (DefaultListAdapter) arg;
            return (Iterable<Object>) defaultListAdapter.getWrappedObject();
        } else {
            throw new WindupException("Unrecognized type passed to: " + getMethodName() + ": "
                    + arg.getClass().getCanonicalName());
        }
    }

    @Override
    public void setContext(GraphRewrite event) {
        // noop
    }
}
