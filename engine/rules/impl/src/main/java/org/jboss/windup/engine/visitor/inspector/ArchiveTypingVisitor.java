package org.jboss.windup.engine.visitor.inspector;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.GraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.dao.ArchiveDao;
import org.jboss.windup.graph.model.resource.ArchiveResourceModel;
import org.jboss.windup.graph.model.resource.EarArchiveModel;
import org.jboss.windup.graph.model.resource.JarArchiveModel;
import org.jboss.windup.graph.model.resource.WarArchiveModel;
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
    private GraphUtil graphUtil;
    
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
        for (ArchiveResourceModel archive : archiveDao.getAll())
        {
            visitArchive(archive);
        }
    }

    @Override
    public void visitArchive(ArchiveResourceModel file)
    {
        // now, check to see whether it is a JAR, and republish the typed value.
        String filePath = file.asFile().getAbsolutePath();

        if (StringUtils.endsWith(filePath, ".jar"))
        {
            graphUtil.addTypeToModel(file, JarArchiveModel.class);
        }
        else if (StringUtils.endsWith(filePath, ".war"))
        {
            graphUtil.addTypeToModel(file, WarArchiveModel.class);
        }
        else if (StringUtils.endsWith(filePath, ".ear"))
        {
            graphUtil.addTypeToModel(file, EarArchiveModel.class);
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
