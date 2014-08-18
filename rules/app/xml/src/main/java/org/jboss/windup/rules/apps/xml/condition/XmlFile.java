package org.jboss.windup.rules.apps.xml.condition;

import java.util.HashMap;
import java.util.Map;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderFind;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.rules.apps.xml.model.XmlResourceModel;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;

public class XmlFile extends GraphCondition
{
    protected static final String UNPARSEABLE_XML_CLASSIFICATION = "Unparseable XML File";
    protected static final String UNPARSEABLE_XML_DESCRIPTION = "This file could not be parsed via XPath";
    private String variable = Iteration.DEFAULT_VARIABLE_LIST_STRING;;

    private final String xpath;
    private Map<String, String> namespaces = new HashMap<>();

    private XmlFile(String xpath)
    {
        this.xpath = xpath;
    }

    /**
     * Create a new {@link XmlFile} {@link Condition}.
     */
    public static XmlFile matchesXpath(String xpath)
    {
        return new XmlFile(xpath);
    }

    public ConditionBuilder as(String variable)
    {
        Assert.notNull(variable, "Variable name must not be null.");
        this.variable = variable;
        return this;
    }

    @Override
    public boolean evaluate(final GraphRewrite event, final EvaluationContext context)
    {
        QueryBuilderFind query = Query.find(XmlResourceModel.class);
        return query.filteredBy(new Predicate<XmlResourceModel>()
        {

            @Override
            public boolean accept(XmlResourceModel xml)
            {
                try
                {
                    Document document = xml.asDocument();
                    String result = XmlUtil.xpathExtract(document, xpath, namespaces);
                    if (result != null)
                        return true;
                }
                catch (Exception e)
                {
                    Service<ClassificationModel> classificationService = event.getGraphContext().getService(
                                ClassificationModel.class);

                    ClassificationModel classification = classificationService.getUniqueByProperty(
                                ClassificationModel.PROPERTY_CLASSIFICATION, XmlFile.UNPARSEABLE_XML_CLASSIFICATION);

                    if (classification == null)
                    {
                        classification = classificationService.create();
                        classification.setDescription(XmlFile.UNPARSEABLE_XML_DESCRIPTION);
                        classification.setClassifiation(XmlFile.UNPARSEABLE_XML_CLASSIFICATION);

                        // TODO replace this with a link to a RuleModel, if that gets implemented.
                        classification.setRuleID(((Rule) context.get(Rule.class)).getId());
                    }

                    classification.addFileModel(xml);
                }

                return false;
            }
        }).as(variable).evaluate(event, context);
    }

    public XmlFile namespace(String prefix, String url)
    {
        namespaces.put(prefix, url);
        return this;
    }

}
