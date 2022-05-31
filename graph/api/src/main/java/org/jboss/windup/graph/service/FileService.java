package org.jboss.windup.graph.service;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.janusgraph.core.attribute.Text;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.TitanUtil;
import org.jboss.windup.graph.frames.FramedVertexIterable;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.util.ExecutionStatistics;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FileService extends GraphService<FileModel> {
    public FileService(GraphContext context) {
        super(context, FileModel.class);
    }

    public FileModel createByFilePath(String filePath) {
        return createByFilePath(null, filePath);
    }

    public FileModel createByFilePath(FileModel parentFile, String filePath) {
        ExecutionStatistics.get().begin("FileService.createByFilePath(parentFile, filePath)");
        // always search by absolute path
        String absolutePath = Paths.get(filePath).normalize().toAbsolutePath().toString();
        FileModel entry = findByPath(absolutePath);

        if (entry == null) {
            entry = this.create();
            entry.setFilePath(absolutePath);
            entry.setParentFile(parentFile);
        }

        if (entry.getParentFile() == null && parentFile != null) {
            // Deal with an odd corner case, that probably only happens in my test environment.
            entry.setParentFile(parentFile);
        }

        ExecutionStatistics.get().end("FileService.createByFilePath(parentFile, filePath)");
        return entry;
    }

    public FileModel findByPath(String filePath) {
        // make the path absolute (as we only store absolute paths)
        filePath = Paths.get(filePath).toAbsolutePath().toString();
        return getUniqueByProperty(FileModel.FILE_PATH, filePath);
    }

    public Iterable<FileModel> findByFilenameRegex(String filenameRegex) {
        filenameRegex = TitanUtil.titanifyRegex(filenameRegex);
        Iterable<Vertex> vertices = getGraphContext().getGraph()
                .traversal()
                .V()
                .has(FileModel.FILE_NAME, Text.textRegex(filenameRegex))

                // I'm not sure why this is necessary, but for some reason ".has(WindupFrame.TYPE_PROP, FileModel.TYPE)"
                // seems to be unreliable with this query. Doing it with this filter seems to fix it.
                .filter(traversal -> {
                    Iterator<VertexProperty<String>> typeIterator = traversal.get().properties(WindupFrame.TYPE_PROP);
                    while (typeIterator.hasNext()) {
                        if (FileModel.TYPE.equals(typeIterator.next().value())) {
                            return true;
                        }
                    }
                    return false;
                })
                .toList();
        return new FramedVertexIterable<>(getGraphContext().getFramed(), vertices, FileModel.class);
    }

    public List<FileModel> findArchiveEntryWithExtension(String... values) {
        // build regex
        if (values.length == 0)
            return Collections.emptyList();

        final String regex;
        if (values.length == 1) {
            regex = ".+\\." + values[0] + "$";
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("\\b(");
            for (String value : values) {
                builder.append("|");
                builder.append(value);
            }
            builder.append(")\\b");
            regex = ".+\\." + builder.toString() + "$";
        }

        return (List<FileModel>) getGraphContext().getQuery(FileModel.class).traverse(g -> g.has("filePath", Text.textRegex(regex)))
                .toList(FileModel.class);
    }
}
