package org.jboss.windup.rules.apps.xml.operation.xslt;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.inject.Inject;
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

import org.apache.commons.lang.StringUtils;
import org.jboss.forge.furnace.util.ClassLoaders;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphApiCompositeClassLoaderProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.LinkModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.model.XsltTransformationModel;
import org.jboss.windup.rules.apps.xml.service.XsltTransformationService;
import org.jboss.windup.rules.files.model.FileReferenceModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.logging.Logger;

import org.jboss.windup.util.Logging;

/**
 * Graph operation doing the xslt transformation using the .xslt source on the target xml object
 * @author mbriskar
 */
public class XSLTTransformation extends AbstractIterationOperation<XmlFileModel> implements XSLTTransformationDescription,XSLTTransformationExtension,XSLTTransformationParams,XSLTTransformationLocation,XSLTTransformationFileSystem
{
    private ClassLoader contextClassLoader;

    @Inject
    GraphApiCompositeClassLoaderProvider compositeClassLoader;

    private static final Logger LOG = Logging.get(XSLTTransformation.class);

    private String description;
    private String template;
    private String extension;

    private Transformer xsltTransformer;

    private Map<String, String> xsltParameters;

    XSLTTransformation(String variable)
    {
        super(variable);
    }

    XSLTTransformation()
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
        return new XSLTTransformationOf(variable);
    }

    /**
     * Set the description of this {@link XSLTTransformation}.
     */
    public XSLTTransformationDescription withDescription(String description)
    {
        this.description = description;
        return this;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    /**
     * Set the extension of this {@link XSLTTransformation}.
     */
    public XSLTTransformationExtension withExtension(String extension)
    {
        this.extension = extension;
        return this;
    }

    /**
     * Set the location of the source XSLT file.
     */
    public static XSLTTransformationLocation using(String location)
    {
        return using(location, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Set the location of the source XSLT file to a absolute path on the filesystem
     */
    public static XSLTTransformationFileSystem usingFilesystem(String location)
    {
        XSLTTransformation tansformation = new XSLTTransformation();
        tansformation.template = location;
        return tansformation;
    }

    /**
     * Set the location of the source XSLT file and set it to use the provided {@link ClassLoader} for resource lookup.
     */
    public static XSLTTransformationLocation using(String location, ClassLoader classLoader)
    {
        XSLTTransformation tansformation = new XSLTTransformation();
        // classLoader instance needed to see the file passed in the location
        tansformation.contextClassLoader = classLoader;
        tansformation.template = location;
        return tansformation;
    }

    private InputStream openInputStream() throws IOException
    {
        if (this.contextClassLoader != null)
        {
            return contextClassLoader.getResourceAsStream(template);
        }
        else
        {
            return new FileInputStream(this.template);
        }
    }

    private void setup()
    {
        try (InputStream resourceAsStream = openInputStream())
        {
            final Source xsltSource = new StreamSource(resourceAsStream);
            final TransformerFactory tf = TransformerFactory.newInstance();
            tf.setURIResolver(new URIResolver()
            {
                @Override
                public Source resolve(String href, String base) throws TransformerException
                {
                    // fetch local only, for speed reasons.
                    if (StringUtils.contains(href, "http://"))
                    {
                        LOG.warning("Trying to fetch remote URL for XSLT.  This is not possible; for speed reasons: "
                                    + href + ": " + base);
                        return null;
                    }
                    return new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(href));
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
                    graphContext,
                    XsltTransformationModel.class);
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
            transformation.setExtension(extension);
            transformation.setSourceLocation(template);
            transformation.setSourceFile(payload);
            transformation.setResult(fileName);

            ClassificationService classificationService = new ClassificationService(graphContext);
            ClassificationModel classificationModel = classificationService.create();
            classificationModel.addFileModel(payload);

            GraphService<LinkModel> linkService = new GraphService<>(graphContext, LinkModel.class);
            LinkModel linkModel = linkService.create();
            linkModel.setDescription(description);
            linkModel.setLink(XsltTransformationService.TRANSFORMEDXML_DIR_NAME + "/" + fileName);
            classificationModel.addLink(linkModel);
        }
        catch (TransformerException e)
        {
            LOG.log(Level.SEVERE, "Exception transforming XML.", e);
        }
    }

    public XSLTTransformationParams withParameters(Map<String, String> parameters)
    {
        this.xsltParameters = parameters;
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
