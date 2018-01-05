package org.jboss.windup.rules.files;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.tinkerpop.gremlin.process.traversal.step.util.event.MutationListener;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;

/**
 * Listens to changes to the "filePath" graph node property, and if it finds it matching some of patterns, attaches the respective types to the
 * vertex. E.g. .*\.tld$ >> XmlFileModel
 *
 * @see FileMappingHandler: <file-mapping from=".*\.tld$" to="XmlFileModel" />
 */
public class FileMappingGraphChangedListener implements MutationListener
{
    private static final Logger LOG = Logger.getLogger(FileMappingGraphChangedListener.class.getName());

    private final GraphRewrite event;

    public FileMappingGraphChangedListener(GraphRewrite event)
    {
        this.event = event;
    }

    @Override
    public void vertexPropertyChanged(Vertex element, Property oldValue, Object setValue, Object... vertexPropertyKeyValues)
    {
        System.out.println("--------------------------------------------------");
        System.out.println("--------------------------------------------------");
        System.out.println("--------------------------------------------------");
        System.out.println("Vertex property changed!!!! - " + element);
        System.out.println("--------------------------------------------------");
        System.out.println("--------------------------------------------------");
        System.out.println("--------------------------------------------------");

        String key = oldValue.key();
        if (!FileModel.FILE_PATH.equals(key))
            return;

        FileService fileService = new FileService(event.getGraphContext());
        FileModel model = fileService.frame(element);

        if (model.isDirectory())
            return;

        Map<String, List<Class<? extends WindupVertexFrame>>> mappings = FileMapping.getMappings(event);

        // Compare the value being set to "fileType" against file mapping patterns.
        // If it matches, add the vertex types to this vertex.
        for (Entry<String, List<Class<? extends WindupVertexFrame>>> entry : mappings.entrySet())
        {
            String pattern = entry.getKey();
            List<Class<? extends WindupVertexFrame>> types = entry.getValue();

            if (((String) setValue).matches(pattern))
            {
                for (Class<? extends WindupVertexFrame> type : types)
                {
                    GraphService.addTypeToModel(event.getGraphContext(), model, type);
                }
                LOG.fine("Mapped file [" + model.getFilePath() + "] matching pattern [" + pattern + "] "
                            + "to the following [" + types.size() + "] types: " + types);
            }
        }
    }

    @Override
    public void vertexPropertyPropertyChanged(VertexProperty element, Property oldValue, Object setValue)
    {

    }

    @Override
    public void vertexAdded(Vertex vertex)
    {

    }

    @Override
    public void vertexRemoved(Vertex vertex)
    {

    }

    @Override
    public void vertexPropertyRemoved(VertexProperty vertexProperty)
    {

    }

    @Override
    public void edgeAdded(Edge edge)
    {

    }

    @Override
    public void edgeRemoved(Edge edge)
    {

    }

    @Override
    public void edgePropertyChanged(Edge element, Property oldValue, Object setValue)
    {

    }

    @Override
    public void edgePropertyRemoved(Edge element, Property property)
    {

    }

    @Override
    public void vertexPropertyPropertyRemoved(VertexProperty element, Property property)
    {

    }
}
