package org.jboss.windup.rules.apps.xml.rules;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

/**
 * Created by mbriskar on 1/11/16.
 */
public class AbstractXsdValidationTest
{
    public static final String VALID_XML = "src/test/resources/xsd-validation/example-pom.xml";
    public static String NOT_VALID_XML = "src/test/resources/xsd-validation/not-xsd-valid.xml";
    public static final String NOT_VALID_XSD_SCHEMA_URL="src/test/resources/xsd-validation/xsd-url-not-exist.xml";
    public static final String NO_XSD_SCHEMA_URL="src/test/resources/xsd-validation/no-xsd-url.xml";

    public void addFileModel(GraphContext context, String filePath) {
        FileModel fileModel = context.getFramed().addVertex(null, XmlFileModel.class);
        String fileName = parseFileName(filePath);

        fileModel.setFilePath(filePath);
        fileModel.setFileName(fileName);
    }

    public String parseFileName(String filePath) {
        String[] filePathSplitted = filePath.split("/");
        String fileName = filePathSplitted[filePathSplitted.length-1];
        return fileName;
    }
}
