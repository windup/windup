package org.jboss.windup.rules.apps.java.scan.ast;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.service.TypeReferenceService;
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

    @Test
    public void testVisitorBasic()
    {
        Assert.assertNotNull(context);
        context.init(null);
        Assert.assertNotNull(visitor);

        FileModel fileModel = context.getFramed().addVertex(null, FileModel.class);
        fileModel.setFilePath("src/test/java/org/jboss/windup/rules/apps/java/scan/ast/VariableResolvingASTVisitorTest.java");

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
        CompilationUnit cu1 = (CompilationUnit) parser.createAST(null);

        TypeInterestFactory.registerInterest(".*"); // for this test, we care about all references
        visitor.init(cu1, fileModel);

        CompilationUnit cu = cu1;
        cu.accept(visitor);

        TypeReferenceService typeRefService = new TypeReferenceService(context);
        Iterable<TypeReferenceModel> references = typeRefService.findAll();

        Assert.assertTrue(references.iterator().hasNext());

        JavaClassSource testSource = Roaster.parse(JavaClassSource.class, fileModel.asInputStream());
        List<Import> imports = testSource.getImports();

        for (Import imprt : imports)
        {
            boolean found = false;
            for (TypeReferenceModel reference : references)
            {
                if (reference.getSourceSnippit().contains(imprt.getQualifiedName()))
                    found = true;
            }
            Assert.assertTrue(found);
        }
    }
}
