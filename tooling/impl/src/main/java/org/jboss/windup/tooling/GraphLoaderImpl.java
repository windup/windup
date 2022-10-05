package org.jboss.windup.tooling;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Provides an implementation of {@link GraphLoader}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GraphLoaderImpl implements GraphLoader {
    @Inject
    private GraphContextFactory graphContextFactory;
    @Inject
    private ToolingXMLService toolingXMLService;

    @Override
    public ExecutionResults loadResults(Path reportDirectory) throws IOException {
        Path graphDirectory = reportDirectory.resolve(GraphContextFactory.DEFAULT_GRAPH_SUBDIRECTORY);
        try (GraphContext graphContext = graphContextFactory.load(graphDirectory)) {
            return new ExecutionResultsImpl(graphContext, toolingXMLService);
        }
    }
}
