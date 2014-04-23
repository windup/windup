package org.jboss.windup.engine.visitor.inspector;

import java.io.File;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.FileResourceDao;
import org.jboss.windup.graph.model.resource.FileResource;

public class BasicVisitor extends AbstractGraphVisitor
{

    @Inject
    private FileResourceDao fileDao;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.DISCOVERY;
    }
    
    @Override
    public void run()
    {
        File r1 = new File("/Users/lb3/Desktop/custom-application-remote.war");
        FileResource r1g = fileDao.createByFilePath(r1.getAbsolutePath());
        
        //
        // File r2 = new File("/Users/bradsdavis/Projects/migrations/inputs/WindupConfigurations.jar");
        // org.jboss.windup.graph.model.resource.FileResource r2g = fileDao.getByFilePath(r2.getAbsolutePath());
    }
}
