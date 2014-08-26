package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationFilter;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.ProjectModelService;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.util.ZipUtil;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Finds Archives that were not classified as Maven archives/projects, and adds some generic project information for
 * them.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DiscoverNonMavenProjectsRuleProvider extends WindupRuleProvider
{
    @Inject
    private ProjectModelService projectModelService;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
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
            Query.find(ArchiveModel.class)
        ).perform(
            Iteration.over(ArchiveModel.class)
                .when(new AbstractIterationFilter<ArchiveModel>(){
                    @Override
                    public boolean evaluate(GraphRewrite event, EvaluationContext context, ArchiveModel payload)
                    {
                        return payload.getProjectModel() == null;
                    }})
                .perform(
                    new AbstractIterationOperation<ArchiveModel>()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload)
                        {
                            if(payload.getProjectModel() == null)
                            {
                                List<ArchiveModel> hierarchy = new ArrayList<>();
                                
                                ArchiveModel parentArchive = payload;
                                while(parentArchive != null && parentArchive.getProjectModel() == null)
                                {
                                    hierarchy.add(parentArchive);
                                    parentArchive = parentArchive.getParentArchive();
                                }
                                
                                ProjectModel childProjectModel = null;
                                for (ArchiveModel archiveModel : hierarchy)
                                {
                                    ProjectModel projectModel = projectModelService.create();
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
                                    
                                    if(childProjectModel != null)
                                    {
                                        childProjectModel.setParentProject(projectModel);
                                    }
                                    childProjectModel = projectModel;
                                }
                            }
                        }
                    }
                )
            .endIteration()
        );       
        // @formatter:on
    }

}
