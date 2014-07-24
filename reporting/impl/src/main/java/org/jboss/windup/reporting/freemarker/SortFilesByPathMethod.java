package org.jboss.windup.reporting.freemarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.windup.graph.model.comparator.FilePathComparator;
import org.jboss.windup.graph.model.resource.FileModel;

import freemarker.ext.beans.StringModel;
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

    @Override
    public String getMethodName()
    {
        return "sortFilesByPathAscending";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (Iterable<FileModel>)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        @SuppressWarnings("unchecked")
        Iterable<FileModel> fileModelIterable = (Iterable<FileModel>) stringModelArg.getWrappedObject();
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
        return fileModelList;
    }
}
