package org.jboss.windup.rules.apps.xml.condition;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.EvaluationStrategy;
import org.jboss.windup.config.parameters.FrameContext;
import org.jboss.windup.config.parameters.FrameCreationContext;
import org.jboss.windup.config.parameters.ParameterizedGraphCondition;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.xml.condition.validators.XmlCacheValidator;
import org.jboss.windup.rules.apps.xml.condition.validators.XmlFileDtdValidator;
import org.jboss.windup.rules.apps.xml.condition.validators.XmlFileNameValidator;
import org.jboss.windup.rules.apps.xml.condition.validators.XmlFileXpathValidator;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.model.XmlTypeReferenceModel;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.Parameter;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;
import org.ocpsoft.rewrite.util.Maps;

import javax.xml.xpath.XPathExpression;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles matching on {@link XmlFileModel} objects and creating/returning {@link XmlTypeReferenceModel} or {@link XmlFileModel} objects on the matching nodes/whole files.
 */
public class XmlFile extends ParameterizedGraphCondition implements XmlFileDTD, XmlFileIn, XmlFileNamespace, XmlFileResult, XmlFileXpath {
    public XmlFile() {

    }

    XmlCacheValidator cacheValidator = new XmlCacheValidator();
    XmlFileNameValidator fileNameValidator = new XmlFileNameValidator();
    XmlFileDtdValidator dtdValidator = new XmlFileDtdValidator();
    XmlFileXpathValidator xpathValidator = new XmlFileXpathValidator();


    public void setXpathResultMatch(String xpathResultMatch) {
        xpathValidator.setXpathResult(xpathResultMatch);
    }

    private XmlFile(String xpath) {
        setXpath(xpath);
    }

    public String getXpathString() {
        return xpathValidator.getXpathString();
    }

    /**
     * Create a new {@link XmlFile} {@link Condition}.
     */
    public static XmlFileXpath matchesXpath(String xpath) {
        return new XmlFile(xpath);
    }

    public XmlFileDTD andDTDPublicId(String publicIdRegex) {
        this.dtdValidator.setPublicId(publicIdRegex);
        return this;
    }

    /**
     * Create a new {@link XmlFile} that matches on the provided DTD namespace.
     */
    public static XmlFileDTD withDTDSystemId(String dtdNamespace) {
        XmlFile xmlFile = new XmlFile();
        xmlFile.dtdValidator.setSystemId(dtdNamespace);
        return xmlFile;
    }

    public XmlFileDTD andDTDSystemId(String dtdNamespace) {
        dtdValidator.setSystemId(dtdNamespace);
        return this;
    }

    /**
     * Output the results using the provided variable name.
     */
    public ConditionBuilder as(String variable) {
        Assert.notNull(variable, "Variable name must not be null.");
        this.setOutputVariablesName(variable);
        return this;
    }

    /**
     * Scan only files that match the given file name.
     */
    public XmlFileIn inFile(String fileName) {
        this.fileNameValidator.setFileNameRegex(fileName);
        return this;
    }

    /**
     * Only return results that match the given regex.
     */
    public XmlFileResult resultMatches(String regex) {
        this.xpathValidator.setXpathResult(regex);
        return this;
    }

    public XPathExpression getXPathExpression() {
        return this.xpathValidator.getXpathExpression();
    }

    public RegexParameterizedPatternParser getInFilePattern() {
        return this.fileNameValidator.getFileNamePattern();
    }

    public String getPublicId() {
        return this.dtdValidator.getPublicId();
    }

    /**
     * Specify the name of the variables to base this query on.
     *
     * @param fromVariable
     * @return
     */
    public static XmlFileFrom from(String fromVariable) {
        return new XmlFileFrom(fromVariable);
    }

    @Override
    public void setParameterStore(ParameterStore store) {
        this.xpathValidator.setParameterStore(store);
    }

    @Override
    public Set<String> getRequiredParameterNames() {
        Set<String> result = new HashSet<>();
        result.addAll(xpathValidator.getRequiredParamaterNames());
        result.addAll(fileNameValidator.getRequiredParamaterNames());
        return result;
    }

    @Override
    protected String getVarname() {
        return getOutputVariablesName();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean evaluateAndPopulateValueStores(final GraphRewrite event, final EvaluationContext context,
                                                     final FrameCreationContext frameCreationContext) {
        return evaluate(event, context, new XmlFileEvaluationStrategy() {
            private LinkedHashMap<String, List<WindupVertexFrame>> variables;

            @Override
            public void modelMatched() {
                this.variables = new LinkedHashMap<>();
                frameCreationContext.beginNew((Map) this.variables);
            }

            @Override
            @SuppressWarnings("rawtypes")
            public void modelSubmitted(WindupVertexFrame model) {
                Maps.addListValue(this.variables, getVarname(), model);
            }

            @Override
            public boolean submitValue(Parameter<?> parameter, String value) {
                ParameterValueStore valueStore = DefaultParameterValueStore.getInstance(context);
                return valueStore.submit(event, context, parameter, value);
            }

            @Override
            public void modelSubmissionRejected() {
                if (variables != null) {
                    this.variables = null;
                    frameCreationContext.rollback();
                }
            }
        });
    }

    public interface XmlFileEvaluationStrategy extends EvaluationStrategy {
        boolean submitValue(Parameter<?> parameter, String value);
    }

    @Override
    protected boolean evaluateWithValueStore(final GraphRewrite event, final EvaluationContext context, final FrameContext frameContext) {
        boolean result = evaluate(event, context, new XmlFileEvaluationStrategy() {
            @Override
            public void modelMatched() {
            }

            @Override
            public void modelSubmitted(WindupVertexFrame model) {
            }

            @Override
            public boolean submitValue(Parameter<?> parameter, String value) {
                ParameterValueStore valueStore = DefaultParameterValueStore.getInstance(context);
                String existingValue = valueStore.retrieve(parameter);
                if (existingValue == null) {
                    return valueStore.submit(event, context, parameter, value);
                } else {
                    return valueStore.isValid(event, context, parameter, value);
                }
            }

            @Override
            public void modelSubmissionRejected() {
            }
        });

        if (!result)
            frameContext.reject();

        return result;
    }

    private boolean evaluate(final GraphRewrite event, final EvaluationContext context, final XmlFileEvaluationStrategy evaluationStrategy) {
        try {
            ExecutionStatistics.get().begin("XmlFile.evaluate");
            initValidators(event, context, evaluationStrategy);
            final List<WindupVertexFrame> finalResults = new ArrayList<>();
            final GraphContext graphContext = event.getGraphContext();
            Iterable<? extends WindupVertexFrame> startVertices = getStartingVertices(event, graphContext);
            for (WindupVertexFrame iterated : startVertices) {
                XmlFileModel xml = getXmlFileModelFromVertex(iterated);
                if (xmlFilePassRestrictions(event, context, xml)) {
                    registerAndSubmitResultsFor(xml, finalResults, evaluationStrategy, event, context);
                }
            }
            setResults(event, getOutputVariablesName(), finalResults);

            return !finalResults.isEmpty();
        } finally {
            ExecutionStatistics.get().end("XmlFile.evaluate");
        }
    }

    private void initValidators(GraphRewrite event, EvaluationContext context, XmlFileEvaluationStrategy evaluationStrategy) {
        xpathValidator.setEvaluationStrategy(evaluationStrategy);
        xpathValidator.setXmlFileNameValidator(fileNameValidator);
        cacheValidator.clear();
    }

    private Iterable<? extends WindupVertexFrame> getStartingVertices(GraphRewrite event, GraphContext graphContext) {
        GraphService<XmlFileModel> xmlResourceService = new GraphService<>(graphContext, XmlFileModel.class);
        Iterable<? extends WindupVertexFrame> allXmls;
        if (getInputVariablesName() == null || getInputVariablesName().isEmpty()) {
            allXmls = xmlResourceService.findAll();
        } else {
            allXmls = Variables.instance(event).findVariable(getInputVariablesName());
        }
        return allXmls;
    }

    private void registerAndSubmitResultsFor(XmlFileModel xml, List<WindupVertexFrame> results, EvaluationStrategy evaluationStrategy, GraphRewrite event, EvaluationContext context) {
        final List<WindupVertexFrame> xpathResults = xpathValidator.getAndClearResultLocations();
        if (xpathResults.isEmpty()) {
            evaluationStrategy.modelMatched();
            if (fileNameValidator.getFileNamePattern() != null && !fileNameValidator.getFileNamePattern().parse(xml.getFileName()).submit(event, context)) {
                evaluationStrategy.modelSubmissionRejected();
                return;
            }
            evaluationStrategy.modelSubmitted(xml);
            results.add(xml);
        } else {
            //these were already submitted by XpathValidator
            results.addAll(xpathResults);
        }
    }

    private XmlFileModel getXmlFileModelFromVertex(WindupVertexFrame vertexFrame) {
        final XmlFileModel xml;
        if (vertexFrame instanceof FileReferenceModel) {
            xml = (XmlFileModel) ((FileReferenceModel) vertexFrame).getFile();
        } else if (vertexFrame instanceof XmlFileModel) {
            xml = (XmlFileModel) vertexFrame;
        } else {
            throw new WindupException("XmlFile was called on the wrong graph type ( " + vertexFrame.toPrettyString()
                    + ")");
        }
        return xml;
    }

    private boolean xmlFilePassRestrictions(final GraphRewrite event, final EvaluationContext context, XmlFileModel xml) {
        boolean validation = cacheValidator.isValid(event, context, xml);
        validation = validation && fileNameValidator.isValid(event, context, xml);
        validation = validation && dtdValidator.isValid(event, context, xml);
        validation = validation && xpathValidator.isValid(event, context, xml);

        return validation;
    }

    public XmlFileNamespace namespace(String prefix, String url) {
        this.xpathValidator.addNamespace(prefix, url);
        return this;
    }

    public void setXpath(String xpath) {
        this.xpathValidator.setXpathString(xpath);

    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("XmlFile");
        if (getInputVariablesName() != null) {
            builder.append(".inputVariable(" + getInputVariablesName() + ")");
        }
        if (xpathValidator.getXpathString() != null) {
            builder.append(".matches(" + xpathValidator.getXpathString() + ")");
        }
        if (fileNameValidator.getFileNamePattern() != null) {
            builder.append(".inFile(" + fileNameValidator.getFileNamePattern().toString() + ")");
        }
        if (dtdValidator.getPublicId() != null) {
            builder.append(".withDTDPublicId(" + dtdValidator.getPublicId() + ")");
        }
        builder.append(".as(" + getOutputVariablesName() + ")");
        return builder.toString();
    }


}