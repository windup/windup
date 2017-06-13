package org.jboss.windup.rules.apps.java.ip;

import static org.joox.JOOX.$;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.java.model.PropertiesModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.files.condition.FileContent;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Finds files that contain potential hard-coded IP addresses, determined by regular expression.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 */
@RuleMetadata(phase = MigrationRulesPhase.class, tags = {"cloud-readiness"})
public class DiscoverHardcodedIPAddressRuleProvider extends AbstractRuleProvider
{
    private static final String IP_PATTERN = "(?<![\\w.])\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(?![\\w.])";
    private static final Logger LOG = Logger.getLogger(DiscoverHardcodedIPAddressRuleProvider.class.getName());

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder
        .begin()
        .addRule()
        // for all files ending in java, properties, and xml,
        // query for the regular expression {ip}
        .when(FileContent.matches("{ip}").inFileNamed("{*}.{type}"))
        .perform(new AbstractIterationOperation<FileLocationModel>()
        {
            // when a result is found, create an inline hint.
            // reference the inline hint with the hardcoded ip marker so that we can query for it
            // in the hardcoded ip report.
            public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel payload)
            {
                // for all file location models that match the regular expression in the where clause, add
                // the IP Location Model to the graph
                if (InetAddressValidator.getInstance().isValid(payload.getSourceSnippit()))
                {
                    // if the file is a property file, make sure the line isn't commented out.
                    if (ignoreLine(event.getGraphContext(), payload))
                    {
                        return;
                    }

                    if (payload.getFile() instanceof SourceFileModel)
                        ((SourceFileModel) payload.getFile()).setGenerateSourceReport(true);

                    HardcodedIPLocationModel location = GraphService.addTypeToModel(event.getGraphContext(), payload,
                        HardcodedIPLocationModel.class);
                    location.setRuleID(((Rule) context.get(Rule.class)).getId());
                    location.setTitle("Hard-coded IP address");

                    StringBuilder hintBody = new StringBuilder("**Hard-coded IP: ");
                    hintBody.append(payload.getSourceSnippit());
                    hintBody.append("**");

                    hintBody.append("\n\n");
                    hintBody.append("When migrating environments, hard-coded IP addresses may need to be modified or eliminated.");
                    location.setHint(hintBody.toString());
                    //location.setIssueCategory(IssueCategoryRegistry.loadFromGraph(event.getGraphContext(), IssueCategoryRegistry.MANDATORY));
                    location.setIssueCategory(IssueCategoryRegistry.loadFromGraph(event.getGraphContext(), IssueCategoryRegistry.CLOUD_MANDATORY));
                    location.setEffort(1);
                }
            }
        })
        .where("ip").matches(IP_PATTERN)
        .where("type").matches("java|properties|xml")
        .withId(getClass().getSimpleName());
    }

    private boolean ignoreLine(GraphContext context, FileLocationModel model)
    {
        boolean isPropertiesFile = model.getFile() instanceof PropertiesModel;

        int lineNumber = model.getLineNumber();
        LineIterator li = null;
        try
        {
            li = FileUtils.lineIterator(model.getFile().asFile());

            int i = 0;
            while (li.hasNext())
            {
                i++;

                // read the line to memory only if it is the line of interest
                if (i == lineNumber)
                {
                    String line = StringUtils.trim(li.next());
                    // check that it isn't commented.
                    if (isPropertiesFile && StringUtils.startsWith(line, "#"))
                        return true;
                    // WINDUP-808 - Remove matches with "version" or "revision" on the same line
                    else if (StringUtils.containsIgnoreCase(line, "version") || StringUtils.containsIgnoreCase(line, "revision"))
                        return true;
                    else if (isMavenVersionTag(context, model))
                        return true;
                    else
                        return false;
                }
                else if (i < lineNumber)
                {
                    // seek
                    li.next();
                }
                else if (i > lineNumber)
                {
                    LOG.warning("Did not find line: " + lineNumber + " in file: " + model.getFile().getFileName());
                    break;
                }
            }
        }
        catch (IOException | RuntimeException e)
        {
            LOG.log(Level.WARNING, "Exception reading properties from file: " + model.getFile().getFilePath(), e);
        }
        finally
        {
            LineIterator.closeQuietly(li);
        }

        return false;
    }

    private boolean isMavenFile(GraphContext context, FileLocationModel model)
    {
        if (!(model.getFile() instanceof XmlFileModel))
        {
            return false;
        }

        ClassificationService cs = new ClassificationService(context);
        for (ClassificationModel cm : cs.getClassificationByName(model.getFile(), "Maven POM"))
        {
            return true;
        }
        return false;
    }

    /**
     * if this is a maven file, checks to see if "version" tags match the discovered text; if the discovered text does match something in a version
     * tag, it is likely a version, not an IP address
     *
     * @param context
     * @param model
     * @return
     */
    private boolean isMavenVersionTag(GraphContext context, FileLocationModel model)
    {
        if (isMavenFile(context, model))
        {
            Document doc = ((XmlFileModel) model.getFile()).asDocument();
            for (Element elm : $(doc).find("version"))
            {
                String text = StringUtils.trim($(elm).text());
                if (StringUtils.equals(text, model.getSourceSnippit()))
                {
                    return true;
                }
            }
        }
        return false;
    }

}
