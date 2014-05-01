package org.jboss.windup.engine.visitor.inspector;

import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.windup.engine.util.ZipUtil;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.GraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.ApplicationReferenceDao;
import org.jboss.windup.graph.dao.ArchiveDao;
import org.jboss.windup.graph.dao.FileResourceDao;
import org.jboss.windup.graph.model.meta.ApplicationReferenceModel;
import org.jboss.windup.graph.model.resource.ArchiveResourceModel;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes untyped archives, and casts them to the appropriate type within the graph. For nested archives, this also
 * extracts the nested archive for profiling.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class ZipArchiveGraphVisitor extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(ZipArchiveGraphVisitor.class);

    @Inject
    private FileResourceDao fileDao;

    @Inject
    private ArchiveDao archiveDao;

    @Inject
    private ApplicationReferenceDao applicationReferenceDao;
    
    @Override
    public List<Class<? extends GraphVisitor>> getDependencies()
    {
        return super.generateDependencies(BasicVisitor.class);
    }
    
    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.DISCOVERY;
    }

    @Override
    public void run()
    {
        // feed all file visitors...
        Set<String> extensionsSet = ZipUtil.getZipExtensions();
        String[] extensions = ZipUtil.getZipExtensions().toArray(new String[extensionsSet.size()]);
        for (FileResourceModel file : fileDao.findArchiveEntryWithExtension(extensions))
        {
            visitFile(file);
        }
        fileDao.commit();
    }

    @Override
    public void visitFile(FileResourceModel file) {
        //now, check to see whether it is a JAR, and republish the typed value.
        String filePath = file.getFilePath();
        
        if(ZipUtil.endsWithZipExtension(filePath)) {
            ZipFile zipFile = null;         
            try {
                java.io.File reference = new java.io.File(file.getFilePath());
                zipFile = new ZipFile(reference);
                
                //go ahead and make it into an archive.
                
                ArchiveResourceModel archive = archiveDao.create(null);
                
                //mark the archive as a top level archive.
                ApplicationReferenceModel applicationReference = applicationReferenceDao.create();
                applicationReference.setArchive(archive);
                
                archive.setArchiveName(reference.getName());
                archive.setParentResource(file);
            } catch (Exception e) {
                LOG.error("Exception creating zip from: "+filePath, e);
            }
            finally {
                IOUtils.closeQuietly(zipFile);
            }
        }
    }
}
