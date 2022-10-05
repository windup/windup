package org.jboss.windup.rules.apps.javaee.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.JsfSourceFileModel;
import org.jboss.windup.rules.files.condition.FileContent;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers JSF files in the input application.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class)
public class DiscoverJsfSourceFileRuleProvider extends AbstractRuleProvider {

    @Override
    public Configuration getConfiguration(RuleLoaderContext context) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(FileContent.matches("{taglib1}").inFileNamed("{*}.{extension}"))
                .perform(new AbstractIterationOperation<FileLocationModel>() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel payload) {
                        GraphService.addTypeToModel(event.getGraphContext(), payload.getFile(), JsfSourceFileModel.class);
                    }
                })
                .where("taglib1")
                .matches("(java\\.sun\\.com/jsf/)|(xmlns\\.jcp\\.org/jsf)")
                .where("extension")
                .matches("(jsp)|(xhtml)|(jspx)")
                ;
    }
}
