package org.jboss.windup.engine.visitor.inspector;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.ArchiveEntryDao;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.dao.JavaMethodDao;
import org.jboss.windup.graph.model.resource.ArchiveEntryResourceModel;
import org.jboss.windup.graph.model.resource.ArchiveResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For all Java Class entries in the zip, this sets up the JavaClass graph entries.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class JavaClassVisitor extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(JavaClassVisitor.class);

    @Inject
    private JavaClassDao javaClassDao;

    @Inject
    private ArchiveEntryDao archiveEntryDao;

    @Inject
    private JavaMethodDao javaMethodDao;
    
    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.INITIAL_ANALYSIS;
    }

    @Override
    public void run()
    {
        int total = (int) archiveEntryDao.count(archiveEntryDao.findArchiveEntryWithExtension("class"));
        int i = 0;
        for (final ArchiveEntryResourceModel entry : archiveEntryDao.findArchiveEntryWithExtension("class"))
        {
            visitArchiveEntry(entry);
            if (i > 0 && i % 100 == 0)
            {
                LOG.info("Processed: " + i + " of " + total + " Java Classes.");
                archiveEntryDao.commit();
            }
            i++;
        }
        LOG.info("Processed: " + i + " of " + total + " Java Classes.");
        archiveEntryDao.commit();
    }

    @Override
    public void visitArchiveEntry(ArchiveEntryResourceModel entry)
    {
        // now, check to see whether it is a JAR, and republish the typed value.
        ArchiveResourceModel archive = entry.getArchive();

        if (archive == null)
        {
            LOG.warn("Archive should not be null: " + entry.asVertex());
            return;
        }

        try
        {
            ClassParser classParser = new ClassParser(archive.asFile().getAbsolutePath(), entry.getArchiveEntry());
            JavaClass parsed = classParser.parse();
            JavaClassProfiler iv = new JavaClassProfiler(parsed, javaClassDao, javaMethodDao, entry);
            iv.process();
        }
        catch (IOException e)
        {
            LOG.error("Exception reading class.", e);
        }
    }

}
