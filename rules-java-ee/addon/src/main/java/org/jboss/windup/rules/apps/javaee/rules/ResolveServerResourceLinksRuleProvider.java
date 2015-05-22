package org.jboss.windup.rules.apps.javaee.rules;

import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.PreReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.LinkModel;
import org.jboss.windup.reporting.model.association.LinkableModel;
import org.jboss.windup.reporting.service.LinkService;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Links server resources (datasources, jms, etc) to EAP 6 resource setup documentation
 *
 */
public class ResolveServerResourceLinksRuleProvider extends IteratingRuleProvider<JNDIResourceModel>
{
    private static final Logger LOG = Logging.get(ResolveServerResourceLinksRuleProvider.class);

    public ResolveServerResourceLinksRuleProvider()
    {
        super(MetadataBuilder.forProvider(ResolveServerResourceLinksRuleProvider.class, "Resolve Links to Server Documentation")
                    .setPhase(PreReportGenerationPhase.class));
    }
    

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(JNDIResourceModel.class);
    }

    
    @Override
    public void perform(GraphRewrite event, EvaluationContext context, JNDIResourceModel payload)
    {
        if(payload instanceof DataSourceModel) {
            processDataSource(event.getGraphContext(), (DataSourceModel)payload);
        }
    }
    
    private void processDataSource(GraphContext context, DataSourceModel dataSource) {
        LinkService linkService = new LinkService(context);
        LinkableModel linkable = GraphService.addTypeToModel(context, dataSource, LinkableModel.class);
        LOG.info("Resolving links...");
        switch (dataSource.getDatabaseTypeName())
        {
            case "Oracle":
                LOG.info("Added Oracle link...");
                LinkModel eap6OracleLink = linkService.getOrCreate("Oracle DataSource Setup", "https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/Example_Oracle_Datasource.html");
                linkable.addLink(eap6OracleLink);
            default:
                LinkModel eap6Link = linkService.getOrCreate("DataSource Documentation", "https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/chap-Datasource_Management.html");
                linkable.addLink(eap6Link);
        }
    }


    @Override
    public String toStringPerform()
    {
        return "Linking to Server Documentation";
    }
}
