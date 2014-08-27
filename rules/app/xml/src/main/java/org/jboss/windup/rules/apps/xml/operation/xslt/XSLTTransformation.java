package org.jboss.windup.rules.apps.xml.operation.xslt;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
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
import org.jboss.windup.reporting.model.FileReferenceModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.model.XsltTransformationModel;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSLTTransformation extends AbstractIterationOperation<XmlFileModel>
{

    private ClassLoader contextClassLoader;

    @Inject
    GraphApiCompositeClassLoaderProvider compositeClassLoader;

    private static final Logger LOG = LoggerFactory.getLogger(XSLTTransformation.class);

    private String description;
    private String location;
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
     * Set the payload to the fileModel of the given instance even though the variable is not directly of it's type.
     * This is mainly to simplify the creation of the rule, when the FileModel itself is not being iterated but just a
     * model referencing it.
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
    public XSLTTransformation withDescription(String description)
    {
        this.description = description;
        return this;
    }

    public void setSourceLocation(String location)
    {
        this.location = location;
    }

    /**
     * Set the extension of this {@link XSLTTransformation}.
     */
    public XSLTTransformation withExtension(String extension)
    {
        this.extension = extension;
        return this;
    }

    /**
     * Set the location of the source XSLT file.
     */
    public static XSLTTransformation using(String location)
    {
        XSLTTransformation tansformation = new XSLTTransformation();
        // classLoader instance needed to see the file passed in the location
        tansformation.contextClassLoader = Thread.currentThread().getContextClassLoader();
        tansformation.location = location;
        return tansformation;
    }

    public void setup()
    {
        try(InputStream resourceAsStream = contextClassLoader.getResourceAsStream(location))
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
                        LOG.warn("Trying to fetch remote URL for XSLT.  This is not possible; for speed reasons: "
                                    + href + ": " + base);
                        return null;
                    }
                    return new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(href));
                }
            });
            ClassLoaders.executeIn(TransformerFactory.class.getClassLoader(), new Callable<Object>(){

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
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Setting property: " + key + " -> " + xsltParameters.get(key));
                    }
                    xsltTransformer.setParameter(key, xsltParameters.get(key));
                }
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Created XSLT successfully: " + location);
            }
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
        GraphService<XsltTransformationModel> transformationService = new GraphService<XsltTransformationModel>(
                    graphContext,
                    XsltTransformationModel.class);
        String relativeDirectory = StringUtils.substringBeforeLast(payload.getFilePath(), File.separator);
        String fileName = StringUtils.substringAfterLast(payload.getFilePath(), File.separator);

        fileName = StringUtils.replace(fileName, ".", "-");
        fileName = fileName + extension;

        File resultFile = new File(relativeDirectory + File.separator + fileName);

        Source xmlSource = new DOMSource(payload.asDocument());
        Result xmlResult = new StreamResult(resultFile);

        try
        {
            xsltTransformer.transform(xmlSource, xmlResult);
            XsltTransformationModel transformation = transformationService.create();
            transformation.setDescription(description);
            transformation.setExtension(extension);
            transformation.setSourceLocation(location);
            transformation.setSourceFile(payload);
            transformation.setResult(resultFile.getAbsolutePath());

        }
        catch (TransformerException e)
        {
            LOG.error("Exception transforming XML.", e);
        }
    }

}
