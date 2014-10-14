package org.jboss.windup.reporting.renderer;

import info.aduna.iteration.CloseableIteration;

import java.io.FileOutputStream;
import java.nio.file.Path;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.oupls.sail.pg.PropertyGraphSail;

public class GraphRDFRenderer extends AbstractGraphRenderer
{
    @Override
    public void renderGraph(GraphContext context)
    {
        try
        {
            WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(context);
            Path outputFolder = createOutputFolder(configuration, "rdf");
            Path outputFile = outputFolder.resolve("graph.rdf");

            Graph graph = context.getGraph();
            Sail sail = new PropertyGraphSail(graph);
            sail.initialize();

            FileOutputStream fos = new FileOutputStream(outputFile.toFile());
            RDFWriter writer = Rio.createWriter(RDFFormat.NTRIPLES, fos);
            writer.startRDF();

            SailConnection sc = null;
            try
            {
                sc = sail.getConnection();
                CloseableIteration<? extends Namespace, SailException> n = sc.getNamespaces();
                try
                {
                    while (n.hasNext())
                    {
                        Namespace ns = n.next();
                        writer.handleNamespace(ns.getPrefix(), ns.getName());
                    }
                }
                finally
                {
                    n.close();
                }

                CloseableIteration<? extends Statement, SailException> i = sc.getStatements(null, null, null, false);
                try
                {
                    while (i.hasNext())
                    {
                        Statement stmt = i.next();
                        if (stmt.getSubject() != null && stmt.getPredicate() != null && stmt.getObject() != null)
                            writer.handleStatement(stmt);
                    }
                }
                finally
                {
                    i.close();
                }
            }
            finally
            {
                if (sc != null)
                    sc.close();
            }

            writer.endRDF();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
