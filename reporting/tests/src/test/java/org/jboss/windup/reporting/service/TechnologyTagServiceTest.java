package org.jboss.windup.reporting.service;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TechnologyTagServiceTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testFindTechnologyTagsByProject() throws Exception {
        try (GraphContext context = factory.create(true)) {
            ProjectService projectService = new ProjectService(context);
            TechnologyTagService techTagService = new TechnologyTagService(context);
            FileService fileService = new FileService(context);

            ProjectModel parent = projectService.create();
            parent.setName("Parent");
            FileModel fm1 = fileService.create();
            TechnologyTagModel parentTag = techTagService.addTagToFileModel(fm1, "ParentTag", TechnologyTagLevel.INFORMATIONAL);
            parent.addFileModel(fm1);

            ProjectModel child1 = projectService.create();
            child1.setName("child1");
            child1.setParentProject(parent);
            FileModel fm2 = fileService.create();
            TechnologyTagModel child1Tag = techTagService.addTagToFileModel(fm2, "Child1Tag", TechnologyTagLevel.INFORMATIONAL);
            child1.addFileModel(fm2);

            ProjectModel child2 = projectService.create();
            child2.setName("child2");
            child2.setParentProject(parent);
            FileModel fm3 = fileService.create();
            TechnologyTagModel child2Tag = techTagService.addTagToFileModel(fm3, "Child2Tag", TechnologyTagLevel.INFORMATIONAL);
            child2.addFileModel(fm3);

            ProjectModel grandChild1 = projectService.create();
            grandChild1.setName("grandchild1");
            grandChild1.setParentProject(child2);
            FileModel fm4 = fileService.create();
            TechnologyTagModel grandchild1Tag = techTagService.addTagToFileModel(fm4, "GrandChild1Tag", TechnologyTagLevel.INFORMATIONAL);
            grandChild1.addFileModel(fm4);

            ProjectModel child3 = projectService.create();
            child3.setName("child3");
            child3.setParentProject(parent);
            FileModel fm5 = fileService.create();
            TechnologyTagModel child3Tag = techTagService.addTagToFileModel(fm5, "Child3Tag", TechnologyTagLevel.INFORMATIONAL);
            child3.addFileModel(fm5);

            Set<TechnologyTagModel> foundTags = new HashSet<>();
            ProjectModelTraversal traversal = new ProjectModelTraversal(parent);
            for (TechnologyTagModel techTag : techTagService.findTechnologyTagsForProject(traversal)) {
                foundTags.add(techTag);
            }

            Assert.assertTrue(foundTags.contains(parentTag));
            Assert.assertTrue(foundTags.contains(child1Tag));
            Assert.assertTrue(foundTags.contains(child2Tag));
            Assert.assertTrue(foundTags.contains(grandchild1Tag));
            Assert.assertTrue(foundTags.contains(child3Tag));
        }
    }
}
