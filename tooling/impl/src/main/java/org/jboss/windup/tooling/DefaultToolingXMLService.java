package org.jboss.windup.tooling;

import org.jboss.windup.util.exception.WindupException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DefaultToolingXMLService implements ToolingXMLService {
    private static final long serialVersionUID = 1L;

    @Override
    public void serializeResults(ExecutionResults results, OutputStream outputStream) {
        try {
            JAXBContext jaxbContext = getJAXBContext();
            jaxbContext.createMarshaller().marshal(results, outputStream);
        } catch (JAXBException e) {
            throw new WindupException("Error serializing results due to: " + e.getMessage(), e);
        }
    }

    @Override
    public void generateSchema(Path outputPath) {
        try {
            JAXBContext jaxbContext = getJAXBContext();
            SchemaOutputResolver sor = new SchemaOutputResolver() {
                @Override
                public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                    StreamResult result = new StreamResult();
                    result.setSystemId(outputPath.toUri().toString());
                    return result;
                }
            };
            jaxbContext.generateSchema(sor);
        } catch (JAXBException | IOException e) {
            throw new WindupException("Error generating Windup schema due to: " + e.getMessage(), e);
        }
    }

    private JAXBContext getJAXBContext() throws JAXBException {
        return JAXBContext.newInstance(
                org.jboss.windup.tooling.ExecutionResultsImpl.class,
                org.jboss.windup.tooling.data.ClassificationImpl.class,
                org.jboss.windup.tooling.data.HintImpl.class,
                org.jboss.windup.tooling.data.LinkImpl.class,
                org.jboss.windup.tooling.data.QuickfixImpl.class,
                org.jboss.windup.tooling.data.ReportLinkImpl.class,
                org.jboss.windup.tooling.data.IssueCategoryImpl.class);
    }
}
