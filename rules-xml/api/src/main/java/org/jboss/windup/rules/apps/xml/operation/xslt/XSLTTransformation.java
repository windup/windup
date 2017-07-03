package org.jboss.windup.rules.apps.xml.operation.xslt;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.util.ClassLoaders;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.IssueDisplayMode;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.model.XsltTransformationModel;
import org.jboss.windup.rules.apps.xml.service.XsltTransformationService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Graph operation doing the xslt transformation using the .xslt source on the target xml object
 * 
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class XSLTTransformation extends AbstractIterationOperation<XmlFileModel> implements XSLTTransformationDescription,
            XSLTTransformationExtension, XSLTTransformationLocation, XSLTTransformationFileSystem, XSLTTransformationOf
{
    private static final Logger LOG = Logging.get(XSLTTransformation.class);

    private ClassLoader contextClassLoader;
    private String description = "";
    private String template;
    private String extension;
    private int effort = 0;

    private Transformer xsltTransformer;
    private Map<String, String> xsltParameters = new HashMap<>();

    private XSLTTransformation(String variable)
    {
        super(variable);
    }

    private XSLTTransformation()
    {
        super();
    }

    /**
     * Set the payload to the fileModel of the given instance even though the variable is not directly of it's type. This is mainly to simplify the
     * creation of the rule, when the FileModel itself is not being iterated but just a model referencing it.
     *
     */
    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        checkVariableName(event, context);
        WindupVertexFrame payload = resolveVariable(event, getVariableName());
        if (payload instanceof FileReferenceModel)
        {
            FileModel file = ((FileReferenceModel) payload).getFile();
            perform(event, context, (XmlFileModel) file);
        }
        else
        {
            super.perform(event, context);
        }

    }

    public void addXsltParameter(String key, String value)
    {
        xsltParameters.put(key, value);
    }

    /**
     * Create a new transformation for the given ref.
     */
    public static XSLTTransformationOf of(String variable)
    {
        return new XSLTTransformation(variable);
    }

    @Override
    public XSLTTransformationDescription withDescription(String description)
    {
        this.description = description;
        return this;
    }

    @Override
    public XSLTTransformationLocation usingTemplate(String template)
    {
        this.template = template;
        return this;
    }

    @Override
    public XSLTTransformationLocation usingTemplate(String location, ClassLoader loader)
    {
        this.template = location;
        this.contextClassLoader = loader;
        return this;
    }

    @Override
    public XSLTTransformationExtension withExtension(String extension)
    {
        this.extension = extension;
        return this;
    }

    /**
     * Create a new {@link XSLTTransformation} using the given location of the source XSLT file within the current
     * {@link Thread#getContextClassLoader()}.
     */
    public static XSLTTransformationLocation using(String location)
    {
        return using(location, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Create a new {@link XSLTTransformation} using the given location of the source XSLT file path on the file-system.
     */
    public static XSLTTransformationFileSystem usingFilesystem(String location)
    {
        XSLTTransformation tansformation = new XSLTTransformation();
        tansformation.template = location;
        return tansformation;
    }

    /**
     * Create a new {@link XSLTTransformation} using the given location of the source XSLT file within the given {@link ClassLoader}.
     */
    public static XSLTTransformationLocation using(String location, ClassLoader classLoader)
    {
        XSLTTransformation tansformation = new XSLTTransformation();
        /*
         * ClassLoader instance needed to see the file passed in the location
         */
        tansformation.contextClassLoader = classLoader;
        tansformation.template = location;
        return tansformation;
    }

    private InputStream openInputStream(String pathString) throws IOException
    {
        if (this.contextClassLoader != null)
        {
            LOG.fine("Loading Resource " + pathString + " with classloader: " + pathString);
            return contextClassLoader.getResourceAsStream(pathString);
        }
        else
        {
            Path path = Paths.get(pathString);
            if (!Files.isRegularFile(path) && !pathString.equals(this.template))
            {
                // probably a relative file... try to infer it from the original template path
                path = Paths.get(this.template).getParent().resolve(path);
            }

            LOG.fine("Loading File " + path + " with from " + path.toAbsolutePath().normalize().toString());
            return new FileInputStream(path.toFile());
        }
    }

    private void setup()
    {
        try (InputStream resourceAsStream = openInputStream(this.template))
        {
            final Source xsltSource = new StreamSource(resourceAsStream);
            final TransformerFactory tf = TransformerFactory.newInstance();
            tf.setURIResolver(new URIResolver()
            {
                @Override
                public Source resolve(String href, String base) throws TransformerException
                {
                    /*
                     * Fetch local only, for speed reasons.
                     */
                    if (StringUtils.contains(href, "http://"))
                    {
                        LOG.warning("Trying to fetch remote URL for XSLT.  This is not possible; for speed reasons: "
                                    + href + ": " + base);
                        return null;
                    }
                    try
                    {
                        InputStream inputStream = openInputStream(href);
                        return new StreamSource(inputStream);
                    } catch (IOException e)
                    {
                        throw new WindupException("Failed to load template: " + href + " due to: " + e.getMessage(), e);
                    }
                }
            });
            ClassLoaders.executeIn(TransformerFactory.class.getClassLoader(), new Callable<Object>()
            {

                @Override
                public Object call() throws Exception
                {
                    xsltTransformer = tf.newTransformer(xsltSource);
                    return null;
                }

            });
            if (xsltParameters != null)
            {
                for (String key : xsltParameters.keySet())
                {
                    LOG.fine("Setting property: " + key + " -> " + xsltParameters.get(key));
                    xsltTransformer.setParameter(key, xsltParameters.get(key));
                }
            }

            LOG.fine("Created XSLT successfully: " + template);
        }
        catch (TransformerConfigurationException e)
        {
            throw new IllegalStateException("Problem working with xsl file located at " + template
                        + ". Please check if the file really exists.", e);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Not able to initialize the XSLT transformer.", e);
        }
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        setup();
        GraphContext graphContext = event.getGraphContext();
        GraphService<XsltTransformationModel> transformationService = new GraphService<>(
                    graphContext, XsltTransformationModel.class);
        String fileName = payload.getFileName();

        fileName = StringUtils.replace(fileName, ".", "-");
        fileName = fileName + extension;

        XsltTransformationService xsltTransformationService = new XsltTransformationService(graphContext);
        Path outputPath = xsltTransformationService.getTransformedXSLTPath();

        Path resultPath = outputPath.resolve(fileName);

        Source xmlSource = new DOMSource(payload.asDocument());
        Result xmlResult = new StreamResult(resultPath.toFile());

        try
        {
            xsltTransformer.transform(xmlSource, xmlResult);
            XsltTransformationModel transformation = transformationService.create();
            transformation.setDescription(description);
            transformation.setEffort(effort);
            transformation.setExtension(extension);
            transformation.setSourceLocation(template);
            transformation.setSourceFile(payload);
            transformation.setResult(fileName);

            ClassificationService classificationService = new ClassificationService(graphContext);
            ClassificationModel classificationModel = classificationService.create();
            classificationModel.setClassification("Transformed to: " + description);
            classificationModel.setEffort(effort);
            classificationModel.addFileModel(payload);
            classificationModel.setRuleID(((Rule) context.get(Rule.class)).getId());
            classificationModel.setIssueDisplayMode(IssueDisplayMode.DETAIL_ONLY);

            GraphService<LinkModel> linkService = new GraphService<>(graphContext, LinkModel.class);
            LinkModel linkModel = linkService.create();
            linkModel.setDescription(description);
            linkModel.setLink(XsltTransformationService.TRANSFORMEDXML_DIR_NAME + "/" + fileName);
            payload.addLinkToTransformedFile(linkModel);
            // classificationModel.addLink(linkModel);
        }
        catch (TransformerException e)
        {
            LOG.log(Level.SEVERE, "Exception transforming XML.", e);
        }
    }

    /**
     * Set the parameters associated with this {@link XSLTTransformation};
     */
    public XSLTTransformationEffort withParameters(Map<String, String> parameters)
    {
        this.xsltParameters.putAll(parameters);
        return this;
    }

    /**
     * Set the estimated effort associated with this {@link XSLTTransformation};
     */
    public XSLTTransformation withEffort(int effort)
    {
        this.effort = effort;
        return this;
    }

    public void setContextClassLoader(ClassLoader classLoader)
    {
        this.contextClassLoader = classLoader;
    }

    public String getDescription()
    {
        return description;
    }

    public String getTemplate()
    {
        return template;
    }

    public String getExtension()
    {
        return extension;
    }

    public Map<String, String> getXsltParameters()
    {
        return xsltParameters;
    }

}
