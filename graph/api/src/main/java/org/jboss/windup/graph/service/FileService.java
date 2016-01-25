package org.jboss.windup.graph.service;

import java.nio.file.Paths;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.util.ExecutionStatistics;

import com.thinkaurelius.titan.core.attribute.Text;
import com.thinkaurelius.titan.util.datastructures.IterablesUtil;
import com.tinkerpop.blueprints.GraphQuery;

/**
 * Contains methods for querying and creating {@link FileModel} vertices.
 */
public class FileService extends GraphService<FileModel>
{
    public FileService(GraphContext context)
    {
        super(context, FileModel.class);
    }

    /**
     * Create a file with the given path.
     */
    public FileModel createByFilePath(String filePath)
    {
        return createByFilePath(null, filePath);
    }

    /**
     * Finds all files that match the given sha1Hash.
     */
    public Iterable<FileModel> findBySHA1Hash(String sha1Hash)
    {
        String name = getClass().getName() + ".findBySHA1Hash(sha1Hash)";
        ExecutionStatistics.get().begin(name);
        try
        {
            return findAllByProperty(FileModel.SHA1_HASH, sha1Hash);
        }
        finally
        {
            ExecutionStatistics.get().end(name);
        }
    }

    /**
     * Create a file with the given path and parent.
     */
    public FileModel createByFilePath(FileModel parentFile, String filePath)
    {
        ExecutionStatistics.get().begin("FileService.createByFilePath(parentFile, filePath)");
        // always search by absolute path
        String absolutePath = Paths.get(filePath).normalize().toAbsolutePath().toString();
        FileModel entry = findByPath(absolutePath);

        if (entry == null)
        {
            entry = this.create();
            entry.setFilePath(absolutePath);
            entry.setParentFile(parentFile);
        }

        ExecutionStatistics.get().end("FileService.createByFilePath(parentFile, filePath)");
        return entry;
    }

    /**
     * Find a file by the given path.
     */
    public FileModel findByPath(String filePath)
    {
        // make the path absolute (as we only store absolute paths)
        filePath = Paths.get(filePath).toAbsolutePath().toString();

        GraphQuery query = getGraphContext().getQuery().has(FileModel.FILE_PATH, filePath).hasNot(FileModel.DUPLICATE, true)
                    .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, FileModel.TYPE);

        return getUnique(query);
    }

    /**
     * Finds a file by the provided list of extensions.
     *
     * WARNING: This could be slow.
     */
    public Iterable<FileModel> findFileWithExtension(String... values)
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

        return getGraphContext().getQuery().type(FileModel.class)
                    .has("filePath", Text.REGEX, regex).vertices(FileModel.class);
    }
}
