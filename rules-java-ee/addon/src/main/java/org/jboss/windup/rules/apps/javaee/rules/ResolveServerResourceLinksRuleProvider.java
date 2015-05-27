package org.jboss.windup.rules.apps.javaee.rules;

import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
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
        if(StringUtils.equals("Oracle", dataSource.getDatabaseTypeName()))
        {
            LinkModel eap6OracleLink = linkService.getOrCreate("Oracle DataSource Setup", "https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/Example_Oracle_Datasource.html");
            linkable.addLink(eap6OracleLink);
        }
        else if(StringUtils.equals("MySQL", dataSource.getDatabaseTypeName()))
        {
            LinkModel lnk = linkService.getOrCreate("MySQL DataSource Setup", "https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/Example_MySQL_Datasource1.html");
            linkable.addLink(lnk);
        }
        else if(StringUtils.equals("Postgres", dataSource.getDatabaseTypeName()))
        {
            LinkModel lnk = linkService.getOrCreate("Postgres DataSource Setup", "https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/sect-Example_Datasources.html#Example_PostgreSQL_Datasource1");
            linkable.addLink(lnk);
        }
        else if(StringUtils.equals("SqlServer", dataSource.getDatabaseTypeName()))
        {
            LinkModel lnk = linkService.getOrCreate("SqlServer DataSource Setup", "https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/Example_Microsoft_SQLServer_Datasource1.html");
            linkable.addLink(lnk);
        }
        else if(StringUtils.equals("DB2", dataSource.getDatabaseTypeName()))
        {
            LinkModel lnk = linkService.getOrCreate("DB2 DataSource Setup", "https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/Example_IBM_DB2_Datasource.html");
            linkable.addLink(lnk);
        }
        else if(StringUtils.equals("Sybase", dataSource.getDatabaseTypeName()))
        {
            LinkModel lnk = linkService.getOrCreate("Sybase DataSource Setup", "https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/Example_Sybase_Datasource.html");
            linkable.addLink(lnk);
        }
        
        LinkModel eap6Link = linkService.getOrCreate("DataSource Documentation", "https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/chap-Datasource_Management.html");
        linkable.addLink(eap6Link);
    }


    @Override
    public String toStringPerform()
    {
        return "Linking to Server Documentation";
    }
}
