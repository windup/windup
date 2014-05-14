package org.jboss.windup.engine.visitor.inspector;

import java.io.File;

import javax.inject.Inject;

import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.graph.dao.FileResourceDao;
import org.jboss.windup.graph.model.resource.FileResourceModel;

public class BasicVisitor extends AbstractGraphVisitor
{

    @Inject
    private FileResourceDao fileDao;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }
    
    @Override
    public void run()
    {
        // Runs likely in <project_root>/engine/tests.
        // Naive but better than previous hardcoded path to Brad's Desktop :)
        File r1 = new File("../../test_files/Windup1x-javaee-example.war");
        FileResourceModel r1g = fileDao.createByFilePath(r1.getAbsolutePath());
    }
}
