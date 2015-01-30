package org.jboss.windup.rules.files;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

public class FileMappingGraphChangedListener implements GraphChangedListener
{
    private static final Logger LOG = Logger.getLogger(FileMappingGraphChangedListener.class.getSimpleName());

    private GraphRewrite event;

    public FileMappingGraphChangedListener(GraphRewrite event)
    {
        this.event = event;
    }

    @Override
    public void vertexPropertyChanged(Vertex vertex, String key, Object oldValue, Object setValue)
    {
        if (FileModel.FILE_PATH.equals(key))
        {
            FileService fileService = new FileService(event.getGraphContext());
            FileModel model = fileService.frame(vertex);

            Map<String, List<Class<? extends WindupVertexFrame>>> mappings = FileMapping
                        .getMappings(event);
            for (Entry<String, List<Class<? extends WindupVertexFrame>>> entry : mappings.entrySet())
            {
                String pattern = entry.getKey();
                List<Class<? extends WindupVertexFrame>> types = entry.getValue();

                if (((String) setValue).matches(pattern))
                {
                    if (!model.isDirectory())
                    {
                        for (Class<? extends WindupVertexFrame> type : types)
                        {
                            GraphService.addTypeToModel(event.getGraphContext(), model, type);
                        }
                        LOG.fine("Mapped file [" + model.getFilePath() + "] matching pattern [" + pattern + "] to the following [" + types.size()
                                    + "] types: " + types);
                    }
                }
            }
        }
    }

    @Override
    public void vertexPropertyRemoved(Vertex vertex, String key, Object removedValue)
    {
    }

    @Override
    public void vertexAdded(Vertex vertex)
    {
    }

    @Override
    public void vertexRemoved(Vertex vertex, Map<String, Object> props)
    {
    }

    @Override
    public void edgeAdded(Edge edge)
    {
    }

    @Override
    public void edgePropertyChanged(Edge edge, String key, Object oldValue, Object setValue)
    {
    }

    @Override
    public void edgePropertyRemoved(Edge edge, String key, Object removedValue)
    {
    }

    @Override
    public void edgeRemoved(Edge edge, Map<String, Object> props)
    {
    }

}
