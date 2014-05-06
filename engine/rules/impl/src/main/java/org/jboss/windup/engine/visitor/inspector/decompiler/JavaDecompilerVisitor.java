package org.jboss.windup.engine.visitor.inspector.decompiler;

import java.io.File;
import java.util.Iterator;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.FileResourceDao;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.model.resource.ArchiveEntryResourceModel;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For all Java Class that are "customer packages" that need further investigation, this visitor will decompile the Java
 * class,
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class JavaDecompilerVisitor extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(JavaDecompilerVisitor.class);

    @Inject
    WindupContext context;

    @Inject
    private JavaClassDao javaClassDao;

    @Inject
    private FileResourceDao fileDao;

    private final DecompilerAdapter decompiler;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.INITIAL_ANALYSIS;
    }

    public JavaDecompilerVisitor()
    {
        decompiler = new JadretroDecompilerAdapter();
    }

    @Override
    public void run()
    {
        // count the iterations so that we can commit periodically.
        int count = 1;
        for (org.jboss.windup.graph.model.resource.JavaClassModel candidate : javaClassDao
                    .findClassesLeveragingCandidateBlacklists())
        {
            LOG.info("Processing candidate: " + candidate.getQualifiedName());
            // now, we should see if the class matches the customer package...
            if (!candidate.isCustomerPackage())
            {
                LOG.info("Not customer package: " + candidate.getQualifiedName());
                continue;
            }

            if (candidate.getSource() == null)
            {
                Iterator<ResourceModel> resources = candidate.getResources().iterator();
                if (resources.hasNext())
                {
                    File fileReference = null;
                    // check its type...
                    ResourceModel resource = resources.next();
                    if (resource instanceof ArchiveEntryResourceModel)
                    {
                        ArchiveEntryResourceModel ae = (ArchiveEntryResourceModel) resource;
                        fileReference = ae.asFile();
                    }
                    else if (resource instanceof FileResourceModel)
                    {
                        FileResourceModel fr = (FileResourceModel) resource;
                        fileReference = fr.asFile();
                    }

                    LOG.info("Class File: " + fileReference);
                    String fileName = UUID.randomUUID().toString();
                    File output = new File(FileUtils.getTempDirectory(), fileName);

                    LOG.info("Java Source: " + fileName);
                    decompiler.decompile(candidate.getQualifiedName(), fileReference, output);

                    File outputReference = new File(output, StringUtils.substringAfterLast(
                                candidate.getQualifiedName(), ".") + ".java");
                    LOG.info("Output: " + outputReference.getAbsolutePath());

                    if (!outputReference.exists())
                    {
                        LOG.warn("Expected: " + outputReference.getAbsolutePath() + " but the file doesn't exist.");
                    }

                    FileResourceModel fr = fileDao.create();
                    fr.setFilePath(outputReference.getAbsolutePath());
                    candidate.setSource(fr);

                    if (count % 100 == 0)
                    {
                        fileDao.commit();
                    }
                }
            }
            else
            {
                LOG.warn("No resource associated with the Java class: " + candidate.getQualifiedName());
                continue;
            }
            count++;
        }

        fileDao.commit();
    }

}
