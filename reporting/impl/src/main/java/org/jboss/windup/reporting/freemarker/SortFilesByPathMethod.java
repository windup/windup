package org.jboss.windup.reporting.freemarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.comparator.FilePathComparator;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;

import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModelException;

/**
 * Sorts file paths according to their alphabetical order.
 * 
 * For example:
 * <ul>
 * <li>/foo/bar/baz.class</li>
 * <li>/foo/car/caz.class</li>
 * <li>/foo/hat.class</li>
 * </ul>
 * 
 * Would become:
 * <ul>
 * <li>/foo/hat.class</li>
 * <li>/foo/bar/baz.class</li>
 * <li>/foo/car/caz.class</li>
 * </ul>
 * 
 * Can be called as follows: sortFilesByPathAscending(Iterable<FileModel>)
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class SortFilesByPathMethod implements WindupFreeMarkerMethod
{
    private static final String NAME = "sortFilesByPathAscending";

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes an Iterable<" + FileModel.class.getSimpleName() + "> and returns them, ordered alphabetically.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (Iterable<FileModel>)");
        }
        Iterable<FileModel> fileModelIterable = getList(arguments.get(0));
        List<FileModel> fileModelList = new ArrayList<>();
        for (FileModel fm : fileModelIterable)
        {
            fileModelList.add(fm);
        }

        final FilePathComparator filePathComparator = new FilePathComparator();
        Collections.sort(fileModelList, new Comparator<FileModel>()
        {
            @Override
            public int compare(FileModel o1, FileModel o2)
            {
                return filePathComparator.compare(o1.getFilePath(), o2.getFilePath());
            }
        });

        ExecutionStatistics.get().end(NAME);
        return fileModelList;
    }

    @SuppressWarnings("unchecked")
    private Iterable<FileModel> getList(Object arg) throws TemplateModelException
    {
        if (arg instanceof BeanModel)
        {
            BeanModel beanModel = (BeanModel) arg;
            return (Iterable<FileModel>) beanModel.getWrappedObject();
        }
        else if (arg instanceof SimpleSequence)
        {
            SimpleSequence simpleSequence = (SimpleSequence) arg;
            return (Iterable<FileModel>) simpleSequence.toList();
        }
        else
        {
            throw new WindupException("Unrecognized type passed to: " + getMethodName() + ": "
                        + arg.getClass().getCanonicalName());
        }
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        // noop
    }
}
