package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;

import com.thinkaurelius.titan.core.attribute.Text;
import com.thinkaurelius.titan.util.datastructures.IterablesUtil;

public class FileModelService extends GraphService<FileModel>
{
    public FileModelService()
    {
        super(FileModel.class);
    }

    public FileModelService(GraphContext context)
    {
        super(context, FileModel.class);
    }

    public FileModel createByFilePath(String filePath)
    {
        FileModel entry = findByPath(filePath);

        if (entry == null)
        {
            entry = this.create();
            entry.setFilePath(filePath);
            getGraphContext().getGraph().commit();
        }

        return entry;
    }

    public FileModel findByPath(String filePath)
    {
        return getUniqueByProperty(FileModel.PROPERTY_FILE_PATH, filePath);
    }

    public Iterable<FileModel> findArchiveEntryWithExtension(String... values)
    {
        // build regex
        if (values.length == 0)
        {
            return IterablesUtil.emptyIterable();
        }

        final String regex;
        if (values.length == 1)
        {
            regex = ".+\\." + values[0] + "$";
        }
        else
        {
            StringBuilder builder = new StringBuilder();
            builder.append("\\b(");
            for (String value : values)
            {
                builder.append("|");
                builder.append(value);
            }
            builder.append(")\\b");
            regex = ".+\\." + builder.toString() + "$";
        }

        return getGraphContext().getFramed().query().has("type", Text.CONTAINS, getTypeValueForSearch())
                    .has("filePath", Text.REGEX, regex).vertices(getType());
    }
}
