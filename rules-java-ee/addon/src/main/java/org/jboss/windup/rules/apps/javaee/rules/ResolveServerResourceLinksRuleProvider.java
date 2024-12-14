package org.jboss.windup.rules.apps.javaee.rules;


import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PreReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.LinkService;
import org.jboss.windup.reporting.model.association.LinkableModel;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationModel;
import org.jboss.windup.rules.apps.javaee.model.ThreadPoolModel;
import org.jboss.windup.rules.apps.javaee.service.JNDIResourceService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Links server resources (datasources, jms, etc) to EAP 6 resource setup documentation
 */
@RuleMetadata(phase = PreReportGenerationPhase.class, id = "Resolve Links to Server Documentation")
public class ResolveServerResourceLinksRuleProvider extends AbstractRuleProvider {
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(when())
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        processServerResources(event.getGraphContext());
                    }

                    @Override
                    public String toString() {
                        return "ResolveServerResourceLinksRule";
                    }
                });
    }


    protected ConditionBuilder when() {
        return Query.fromType(JNDIResourceModel.class).or(Query.fromType(ThreadPoolModel.class));
    }

    protected void processServerResources(GraphContext context) {
        JNDIResourceService jndiService = new JNDIResourceService(context);
        GraphService<ThreadPoolModel> threadPoolService = new GraphService<>(context, ThreadPoolModel.class);

        for (JNDIResourceModel model : jndiService.findAll()) {
            processJndiResource(context, model);
        }

        for (ThreadPoolModel model : threadPoolService.findAll()) {
            processThreadPool(context, model);
        }
    }

    protected void processJndiResource(GraphContext context, JNDIResourceModel payload) {
        if (payload instanceof DataSourceModel) {
            processDataSource(context, (DataSourceModel) payload);
        } else if (payload instanceof JmsDestinationModel) {
            processJMSDestination(context, (JmsDestinationModel) payload);
        }
    }

    private void processJMSDestination(GraphContext context, JmsDestinationModel destination) {
        LinkService linkService = new LinkService(context);
        LinkableModel linkable = GraphService.addTypeToModel(context, destination, LinkableModel.class);

        LinkModel jmsDestinationLink = linkService
                .getOrCreate(
                        "Destination Setup",
                        "https://access.redhat.com/documentation/en-US/red_hat_JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/sect-Configuration1.html");
        linkable.addLink(jmsDestinationLink);
    }

    private void processThreadPool(GraphContext context, ThreadPoolModel threadPool) {
        LinkService linkService = new LinkService(context);
        LinkableModel linkable = GraphService.addTypeToModel(context, threadPool, LinkableModel.class);

        LinkModel jmsDestinationLink = linkService
                .getOrCreate(
                        "Thread Pool Setup",
                        "https://access.redhat.com/documentation/en-US/red_hat_JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/sect-Configuring_EJB_Thread_Pools.html");
        linkable.addLink(jmsDestinationLink);
    }

    private void processDataSource(GraphContext context, DataSourceModel dataSource) {
        LinkService linkService = new LinkService(context);
        LinkableModel linkable = GraphService.addTypeToModel(context, dataSource, LinkableModel.class);
        if (StringUtils.equals("Oracle", dataSource.getDatabaseTypeName())) {
            LinkModel eap6OracleLink = linkService
                    .getOrCreate(
                            "Oracle DataSource Setup",
                            "https://access.redhat.com/documentation/en-US/red_hat_JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/sect-Example_Datasources.html#Example_Oracle_Datasource");
            linkable.addLink(eap6OracleLink);
        } else if (StringUtils.equals("MySQL", dataSource.getDatabaseTypeName())) {
            LinkModel lnk = linkService
                    .getOrCreate(
                            "MySQL DataSource Setup",
                            "https://access.redhat.com/documentation/en-US/red_hat_JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/sect-Example_Datasources.html#Example_MySQL_Datasource1");
            linkable.addLink(lnk);
        } else if (StringUtils.equals("Postgres", dataSource.getDatabaseTypeName())) {
            LinkModel lnk = linkService
                    .getOrCreate(
                            "Postgres DataSource Setup",
                            "https://access.redhat.com/documentation/en-US/red_hat_JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/sect-Example_Datasources.html#Example_PostgreSQL_Datasource1");
            linkable.addLink(lnk);
        } else if (StringUtils.equals("SqlServer", dataSource.getDatabaseTypeName())) {
            LinkModel lnk = linkService
                    .getOrCreate(
                            "SqlServer DataSource Setup",
                            "https://access.redhat.com/documentation/en-US/red_hat_JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/sect-Example_Datasources.html#Example_Microsoft_SQLServer_Datasource1");
            linkable.addLink(lnk);
        } else if (StringUtils.equals("DB2", dataSource.getDatabaseTypeName())) {
            LinkModel lnk = linkService
                    .getOrCreate(
                            "DB2 DataSource Setup",
                            "https://access.redhat.com/documentation/en-US/red_hat_JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/sect-Example_Datasources.html#Example_IBM_DB2_Datasource");
            linkable.addLink(lnk);
        } else if (StringUtils.equals("Sybase", dataSource.getDatabaseTypeName())) {
            LinkModel lnk = linkService
                    .getOrCreate(
                            "Sybase DataSource Setup",
                            "https://access.redhat.com/documentation/en-US/red_hat_JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/sect-Example_Datasources.html#Example_Sybase_Datasource");
            linkable.addLink(lnk);
        }

        LinkModel eap6Link = linkService
                .getOrCreate(
                        "DataSource Documentation",
                        "https://access.redhat.com/documentation/en-US/red_hat_JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/chap-Datasource_Management.html");
        linkable.addLink(eap6Link);
    }

}
