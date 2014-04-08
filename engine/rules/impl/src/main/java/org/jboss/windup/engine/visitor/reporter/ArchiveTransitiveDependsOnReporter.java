package org.jboss.windup.engine.visitor.reporter;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.JarArchiveDao;
import org.jboss.windup.graph.model.resource.JarArchive;
import org.jboss.windup.graph.renderer.SimpleGraphRenderer;
import org.jboss.windup.graph.renderer.SimpleGraphRenderer.RenderableVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For each JAR, recurse the dependencies / provides for to determine transitivity. Depth first implementation.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class ArchiveTransitiveDependsOnReporter extends AbstractGraphVisitor
{

    private static final Logger LOG = LoggerFactory.getLogger(ArchiveTransitiveDependsOnReporter.class);

    @Inject
    private JarArchiveDao jarDao;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.Reporting;
    }

    @Override
    public void run()
    {
        processDependsOnTransitive();
        processProvidesForTransitive();
    }

    public void processDependsOnTransitive()
    {
        for (JarArchive archive : jarDao.getAll())
        {

            SimpleGraphRenderer simpleGraph = new SimpleGraphRenderer("depends on");
            RenderableVertex v = simpleGraph.getFramed().addVertex(null, RenderableVertex.class);
            v.setLabel(archive.getArchiveName());

            LOG.info("Archive: " + archive.getArchiveName());

            Set<Object> vertexIds = new HashSet<Object>();
            profileTransitiveDependsOn(archive, vertexIds);
            vertexIds.remove(archive.asVertex().getId());

            for (Object key : vertexIds)
            {
                JarArchive dep = jarDao.getById(key);
                LOG.info(" - Depends on: " + dep.getArchiveName());

                RenderableVertex child = simpleGraph.getFramed().addVertex(null, RenderableVertex.class);
                child.setLabel(dep.getArchiveName());
                v.addOut(child);
            }
            File dagre = new File(FileUtils.getTempDirectory(), archive.getArchiveName() + "-depends-dagred3.html");
            File sigma = new File(FileUtils.getTempDirectory(), archive.getArchiveName() + "-depends-sigmajs.html");
            File vizjs = new File(FileUtils.getTempDirectory(), archive.getArchiveName() + "-depends-vizjs.html");
            simpleGraph.renderDagreD3(dagre);
            simpleGraph.renderSigma(sigma);
            simpleGraph.renderVizjs(vizjs);
            LOG.info("Created graph: " + dagre.getAbsolutePath());
            LOG.info("Created graph: " + sigma.getAbsolutePath());
            LOG.info("Created graph: " + vizjs.getAbsolutePath());
        }
    }

    public void processProvidesForTransitive()
    {
        for (JarArchive archive : jarDao.getAll())
        {
            SimpleGraphRenderer simpleGraph = new SimpleGraphRenderer("provides for");
            RenderableVertex v = simpleGraph.getFramed().addVertex(null, RenderableVertex.class);
            v.setLabel(archive.getArchiveName());

            LOG.info("Archive: " + archive.getArchiveName());

            Set<Object> vertexIds = new HashSet<Object>();
            profileTransitiveProvidesFor(archive, vertexIds);
            vertexIds.remove(archive.asVertex().getId());

            for (Object key : vertexIds)
            {
                JarArchive dep = jarDao.getById(key);
                LOG.info(" - Provides for: " + dep.getArchiveName());

                RenderableVertex child = simpleGraph.getFramed().addVertex(null, RenderableVertex.class);
                child.setLabel(dep.getArchiveName());
                v.addIn(child);
            }

            File dagre = new File(FileUtils.getTempDirectory(), archive.getArchiveName() + "-provides-dagred3.html");
            File sigma = new File(FileUtils.getTempDirectory(), archive.getArchiveName() + "-provides-sigmajs.html");
            File vizjs = new File(FileUtils.getTempDirectory(), archive.getArchiveName() + "-provides-vizjs.html");
            simpleGraph.renderDagreD3(dagre);
            simpleGraph.renderSigma(sigma);
            simpleGraph.renderVizjs(vizjs);
            LOG.info("Created graph: " + dagre.getAbsolutePath());
            LOG.info("Created graph: " + sigma.getAbsolutePath());
            LOG.info("Created graph: " + vizjs.getAbsolutePath());
        }
    }

    // depth first transversal.
    public void profileTransitiveDependsOn(JarArchive archive, Set<Object> visited)
    {
        visited.add(archive.asVertex().getId());
        for (JarArchive clz : archive.dependsOnArchives())
        {
            if (!visited.contains(clz.asVertex().getId()))
            {
                profileTransitiveDependsOn(clz, visited);
            }
        }
    }

    // depth first transversal.
    public void profileTransitiveProvidesFor(JarArchive archive, Set<Object> visited)
    {
        visited.add(archive.asVertex().getId());
        for (JarArchive clz : archive.providesForArchives())
        {
            if (!visited.contains(clz.asVertex().getId()))
            {
                profileTransitiveDependsOn(clz, visited);
            }
        }
    }
}
