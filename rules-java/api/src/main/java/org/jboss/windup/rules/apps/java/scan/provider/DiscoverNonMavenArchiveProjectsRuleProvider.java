package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationFilter;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructure;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.util.ZipUtil;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Finds Archives that were not classified as Maven archives/projects, and adds some generic project information for them.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DiscoverNonMavenArchiveProjectsRuleProvider extends WindupRuleProvider
{
    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return DiscoverProjectStructure.class;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(DiscoverMavenProjectsRuleProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext arg0)
    {
        // @formatter:off
        return ConfigurationBuilder.begin()
        .addRule()
        .when(
            Query.fromType(ArchiveModel.class)
        ).perform(
            Iteration.over(ArchiveModel.class)
                .when(new AbstractIterationFilter<ArchiveModel>(){
                    @Override
                    public boolean evaluate(GraphRewrite event, EvaluationContext context, ArchiveModel payload)
                    {
                        return payload.getProjectModel() == null;
                    }
                    @Override
                    public String toString()
                    {
                        return "ProjectModel == null";
                    }
                })
                .perform(
                    new AbstractIterationOperation<ArchiveModel>()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload)
                        {
                            List<ArchiveModel> hierarchy = new ArrayList<>();
                            
                            ArchiveModel parentArchive = payload;
                            while(parentArchive != null)
                            {
                                hierarchy.add(parentArchive);
                                
                                // break once we have added a parent with a project model
                                if (parentArchive.getProjectModel() != null)
                                {
                                    break;
                                }
                                
                                parentArchive = parentArchive.getParentArchive();
                                
                            }
                            
                            ProjectModel childProjectModel = null;
                            ProjectService projectModelService = new ProjectService(event.getGraphContext());
                            for (ArchiveModel archiveModel : hierarchy)
                            {
                                ProjectModel projectModel = archiveModel.getProjectModel();
                                
                                // create the project if we don't already have one
                                if (projectModel == null) {
                                    projectModel = projectModelService.create();
                                    projectModel.setName(archiveModel.getArchiveName());
                                    projectModel.setRootFileModel(archiveModel);
                                    projectModel.setDescription("Unidentified Archive");
                                    
                                    if(ZipUtil.endsWithZipExtension(archiveModel.getArchiveName()))
                                    {
                                        for (String extension : ZipUtil.getZipExtensions())
                                        {
                                            if(archiveModel.getArchiveName().endsWith(extension))
                                                projectModel.setProjectType(extension);
                                        }
                                    }
                                    
                                    archiveModel.setProjectModel(projectModel);
                                    // Attach the project to all files within the archive
                                    for (FileModel f : archiveModel.getContainedFileModels())
                                    {
                                        // don't add archive models, as those really are separate projects...
                                        // also, don't set the project model if one is already set
                                        if (!(f instanceof ArchiveModel) && f.getProjectModel() == null)
                                        {
                                            // only set it if it has not already been set
                                            f.setProjectModel(projectModel);
                                            projectModel.addFileModel(f);
                                        }
                                    }
                                }
                                
                                if(childProjectModel != null)
                                {
                                    childProjectModel.setParentProject(projectModel);
                                }
                                childProjectModel = projectModel;
                            }
                        }
                        
                        public String toString() {
                            return "ScanAsNonMavenProject";
                        }
                    }
                    .and(IterationProgress.monitoring("Checking for non-Maven archive: ", 1))
                )
            .endIteration()
        );       
        // @formatter:on
    }

}
