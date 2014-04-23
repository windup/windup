package org.jboss.windup.engine.visitor.inspector;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.GraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.ArchiveDao;
import org.jboss.windup.graph.dao.EarArchiveDao;
import org.jboss.windup.graph.dao.JarArchiveDao;
import org.jboss.windup.graph.dao.WarArchiveDao;
import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;

/**
 * Determines the extension of the "unknown" archive type, and then casts it to the subtype: JAR, WAR, EAR, etc.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class ArchiveTypingVisitor extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(ArchiveTypingVisitor.class);

    @Inject
    private WarArchiveDao warDao;

    @Inject
    private JarArchiveDao jarDao;

    @Inject
    private EarArchiveDao earDao;

    @Inject
    private ArchiveDao archiveDao;
    
    @Override
    public List<Class<? extends GraphVisitor>> getDependencies()
    {
        return super.generateDependencies(ZipArchiveGraphVisitor.class, DirectoryVisitor.class);
    }
    
    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.DISCOVERY;
    }
    
    @Override
    public void run()
    {
        for (ArchiveResource archive : archiveDao.getAll())
        {
            visitArchive(archive);
        }
    }

    @Override
    public void visitArchive(ArchiveResource file)
    {
        // now, check to see whether it is a JAR, and republish the typed value.
        String filePath = file.asFile().getAbsolutePath();

        if (StringUtils.endsWith(filePath, ".jar"))
        {
            jarDao.castToType(file);
        }
        else if (StringUtils.endsWith(filePath, ".war"))
        {
            warDao.castToType(file);
        }
        else if (StringUtils.endsWith(filePath, ".ear"))
        {
            earDao.castToType(file);
        }
        else
        {
            Vertex v = file.asVertex();
            LOG.info("Not found for Vertex: " + v);
            for (String key : v.getPropertyKeys())
            {
                LOG.info(" - " + key + " -> " + v.getProperty(key));
            }

            LOG.warn("Extension not routed for: " + filePath);
        }
    }

}
