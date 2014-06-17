package org.jboss.windup.rules.apps.java.scan.ast;

import java.io.File;
import java.io.IOException;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.scan.ast.event.JavaScannerASTEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class VariableResolvingASTVisitorTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),

    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Inject
    private VariableResolvingASTVisitor visitor;

    public void observeEvents(@Observes JavaScannerASTEvent event)
    {
        System.out.println("UnitTest Event: " + event);
    }

    @Test
    public void testVisitorBasic()
    {
        Assert.assertNotNull(context);
        Assert.assertNotNull(visitor);

        CompilationUnit cu = initVisitor("src/test/java/org/jboss/windup/rules/apps/java/scan/ast/VariableResolvingASTVisitorTest.java");
        cu.accept(visitor);
    }

    private CompilationUnit initVisitor(String filepath)
    {
        FileModel fileModel = context.getFramed().addVertex(null, FileModel.class);
        fileModel.setFilePath(filepath);

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setBindingsRecovery(true);
        parser.setResolveBindings(true);
        try
        {
            File sourceFile = fileModel.asFile();
            parser.setSource(FileUtils.readFileToString(sourceFile).toCharArray());
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to get source for file: " + fileModel.getFilePath() + " due to: "
                        + e.getMessage(), e);
        }
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        visitor.init(cu, fileModel);
        return cu;
    }
}
