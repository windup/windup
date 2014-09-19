package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.jboss.forge.furnace.util.Strings;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Adds metadata from the .class file itself to the graph.
 * 
 */
public class AddClassFileMetadata extends AbstractIterationOperation<FileModel>
{
    private static final Logger LOG = Logger.getLogger(AddClassFileMetadata.class.getSimpleName());

    private static final String TECH_TAG = "Java Class";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.INFORMATIONAL;

    private AddClassFileMetadata(String variableName)
    {
        super(variableName);
    }

    public AddClassFileMetadata()
    {
        super();
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
    {
        try
        {
            try (FileInputStream fis = new FileInputStream(payload.getFilePath()))
            {
                ClassParser parser = new ClassParser(fis, payload.getFilePath());
                JavaClass javaClass = parser.parse();
                String packageName = javaClass.getPackageName();
                String qualifiedName = javaClass.getClassName();

                JavaClassFileModel classFileModel = GraphService.addTypeToModel(event.getGraphContext(),
                            payload, JavaClassFileModel.class);
                TechnologyTagService techTagService = new TechnologyTagService(event.getGraphContext());
                techTagService.addTagToFileModel(classFileModel, TECH_TAG, TECH_TAG_LEVEL);

                String simpleName = qualifiedName;
                if (packageName != null && !packageName.equals("") && simpleName != null)
                {
                    simpleName = simpleName.substring(packageName.length() + 1);
                }

                classFileModel.setPackageName(packageName);

                JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
                JavaClassModel javaClassModel = javaClassService.getOrCreate(qualifiedName);

                javaClassModel.setSimpleName(simpleName);
                javaClassModel.setPackageName(packageName);
                javaClassModel.setQualifiedName(qualifiedName);
                javaClassModel.setClassFile(classFileModel);

                String[] interfaceNames = javaClass.getInterfaceNames();
                if (interfaceNames != null)
                {
                    for (String iface : interfaceNames)
                    {
                        JavaClassModel interfaceModel = javaClassService.getOrCreate(iface);
                        javaClassModel.addImplements(interfaceModel);
                    }
                }

                String superclassName = javaClass.getSuperclassName();
                if (Strings.isNullOrEmpty(superclassName))
                    javaClassModel.setExtends(javaClassService.getOrCreate(superclassName));

                classFileModel.setJavaClass(javaClassModel);
            }
        }
        catch (Exception e)
        {
            LOG.log(Level.WARNING,
                        "BCEL was unable to parse class file: " + payload.getFilePath() + " due to: " + e.getMessage(),
                        e);
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            classificationService.attachClassification(payload, JavaClassFileModel.UNPARSEABLE_CLASS_CLASSIFICATION,
                        JavaClassFileModel.UNPARSEABLE_CLASS_DESCRIPTION);
        }
    }

    public static OperationBuilder to(String var)
    {
        return new AddClassFileMetadata(var);
    }

}
