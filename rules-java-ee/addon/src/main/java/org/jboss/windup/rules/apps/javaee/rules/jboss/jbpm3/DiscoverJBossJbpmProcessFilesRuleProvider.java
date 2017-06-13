package org.jboss.windup.rules.apps.javaee.rules.jboss.jbpm3;


import static org.joox.JOOX.$;

import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.Jbpm3ProcessModel;
import org.jboss.windup.rules.apps.xml.DiscoverXmlFilesRuleProvider;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Discovers JBoss JBPM XML files and parses the related metadata (processdefinition.xml)
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = DiscoverXmlFilesRuleProvider.class, perform = "Discover JBoss EJB XML Files")
public class DiscoverJBossJbpmProcessFilesRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(DiscoverJBossJbpmProcessFilesRuleProvider.class.getName());

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(XmlFileModel.ROOT_TAG_NAME, "process-definition");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        JavaClassService javaClassService = new JavaClassService(event.getGraphContext());

        if($(payload.asDocument()).find("start-state").isEmpty()) {
            LOG.warning("Found process-definition, but no start-state.");
            return;
        }

        Jbpm3ProcessModel processModel = GraphService.addTypeToModel(event.getGraphContext(), payload, Jbpm3ProcessModel.class);
        Document doc = payload.asDocument();


        //try and read out the process name
        String processName = $(doc).attr("name");
        if(StringUtils.isNotBlank(processName)) {
            processModel.setProcessName(processName);
            LOG.info("Found process: "+processName);
        }
        else {
            LOG.info("Process name is null for process: "+payload.getFilePath());
        }

        //count all nodes
        processModel.setNodeCount($(doc).find("node").get().size());
        processModel.setDecisionCount($(doc).find("decision").get().size());
        processModel.setStateCount($(doc).find("state").get().size());
        processModel.setTaskCount($(doc).find("task").get().size());
        processModel.setSubProcessCount($(doc).find("sub-process").get().size());


        for(Element action : $(doc).find("action").get()) {
            String actionName = $(action).attr("name");
            String className = $(action).attr("class");

            if(StringUtils.isNotBlank(className)) {
                JavaClassModel javaClass = javaClassService.getOrCreatePhantom(className);
                processModel.addActionHandler(javaClass);
            }
        }

        for(Element decision : $(doc).find("decision").get()) {
            for(Element handler : $(decision).find("handler").get()) {
                String className = $(handler).attr("class");

                if(StringUtils.isNotBlank(className)) {
                    JavaClassModel javaClass = javaClassService.getOrCreatePhantom(className);
                    processModel.addDecisionHandler(javaClass);
                }
            }
        }

        //try and find the process image to associate to the definition
        String processImage = payload.getFilePath();
        processImage = StringUtils.removeEnd(processImage, payload.getFileName());
        processImage += "processimage.jpg";

        //look up the process definition.
        FileService fileService = new FileService(event.getGraphContext());
        FileModel processDefinitionImage = fileService.findByPath(processImage);

        if(processDefinitionImage == null) {
            LOG.warning("Expected process definition image at: "+processImage+", but wasn't found.");
        }
        else {
            ReportResourceFileModel reportResource = GraphService.addTypeToModel(event.getGraphContext(), processDefinitionImage, ReportResourceFileModel.class);
            processModel.setProcessImage(reportResource);


            //check to see if there is a processdefinition.xml in the same directory.
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            classificationService.attachClassification(event, context, processDefinitionImage, "JBPM Process Image", "JBPM 3 Process Image.");

            TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
            TechnologyTagModel techTag = technologyTagService.addTagToFileModel(processDefinitionImage, "JBoss Process Image", TechnologyTagLevel.IMPORTANT);
            techTag.setVersion("3");
        }
    }
}
