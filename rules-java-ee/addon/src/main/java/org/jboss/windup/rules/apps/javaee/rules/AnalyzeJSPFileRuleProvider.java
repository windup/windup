package org.jboss.windup.rules.apps.javaee.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestFactory;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.java.service.TypeReferenceService;
import org.jboss.windup.rules.apps.javaee.model.JspSourceFileModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Extracts type references from JSP files.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class AnalyzeJSPFileRuleProvider extends AbstractRuleProvider
{
    public AnalyzeJSPFileRuleProvider()
    {
        super(MetadataBuilder.forProvider(AnalyzeJSPFileRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class)
                    .setHaltOnException(true));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(JspSourceFileModel.class))
                .perform(new ParseSourceOperation());
    }
    // @formatter:on

    private class ParseSourceOperation extends AbstractIterationOperation<JspSourceFileModel>
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, JspSourceFileModel sourceFile)
        {

            TypeReferenceService typeReferenceService = new TypeReferenceService(event.getGraphContext());

            try
            {
                // Setup some basic details about the "Java Class"
                // source root, is decompiled, javaclass. package name
                sourceFile.setPackageName("");
                sourceFile.setDecompiled(false);
                JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
                JavaClassModel classModel = javaClassService.create();
                classModel.setPackageName("");
                classModel.setSimpleName(sourceFile.getFileName());
                classModel.setQualifiedName(sourceFile.getFileName());
                classModel.setExtends(javaClassService.getOrCreatePhantom("javax.servlet.http.HttpServlet"));
                sourceFile.addJavaClass(classModel);

                Iterable<ClassReference> references = getClassReferences(typeReferenceService, sourceFile);
                for (ClassReference reference : references)
                {
                    JavaTypeReferenceModel typeReference = typeReferenceService.createTypeReference(sourceFile,
                                reference.getLocation(),
                                reference.getResolutionStatus(),
                                reference.getLineNumber(), reference.getColumn(), reference.getLength(),
                                reference.getQualifiedName(),
                                reference.getLine());
                }
            }
            catch (Exception e)
            {
                ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                classificationService.attachClassification(context, sourceFile, JspSourceFileModel.UNPARSEABLE_JSP_CLASSIFICATION,
                            JspSourceFileModel.UNPARSEABLE_JSP_DESCRIPTION);
            }
        }
    }

    private Iterable<ClassReference> getClassReferences(TypeReferenceService service, JspSourceFileModel sourceFile) throws IOException
    {
        String source = FileUtils.readFileToString(sourceFile.asFile());

        List<ClassReference> results = new ArrayList<>();
        results.addAll(findImports(source));
        results.addAll(findTaglib(source));
        return results;
    }

    private List<ClassReference> findImports(String source)
    {
        List<ClassReference> results = new ArrayList<>();

        Pattern jspImport = Pattern.compile("<%@\\s*page\\s+[^>]*\\s*import\\s*=\\s*['\"]([^'\"]+)['\"].*?%>",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = jspImport.matcher(source);
        while (matcher.find())
        {
            String matched = matcher.group(1);
            if (StringUtils.isNotBlank(matched))
            {
                String[] imports = StringUtils.split(matched, ",");
                if (imports != null)
                {
                    for (String imported : imports)
                    {
                        imported = StringUtils.trim(imported);
                        if (TypeInterestFactory.matchesAny(imported, TypeReferenceLocation.IMPORT))
                        {
                            ClassReference reference = createClassReference(TypeReferenceLocation.IMPORT, source, imported, matcher.start());
                            results.add(reference);
                        }
                    }
                }
            }
        }
        return results;
    }

    private List<ClassReference> findTaglib(String source)
    {
        List<ClassReference> results = new ArrayList<>();

        Pattern taglibPattern = Pattern.compile("<%@\\s*taglib\\s+[^>]*\\s*uri\\s*=\\s*['\"]([^'\"]+)['\"].*?%>",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = taglibPattern.matcher(source);
        while (matcher.find())
        {
            String matched = matcher.group(1);
            if (StringUtils.isNotBlank(matched))
            {
                if (TypeInterestFactory.matchesAny(matched, TypeReferenceLocation.TAGLIB_IMPORT))
                {
                    ClassReference reference = createClassReference(TypeReferenceLocation.TAGLIB_IMPORT, source, matched, matcher.start());
                    results.add(reference);
                }
            }
        }
        return results;
    }

    private ClassReference createClassReference(TypeReferenceLocation location, String source, String reference, int startPosition)
    {
        String subString = StringUtils.substring(source, 0, startPosition + 1);
        String[] lines = subString.split("\r\n|\r|\n");

        int lineNumber = lines.length;
        int column = lines[lines.length - 1].indexOf(source.substring(startPosition));
        int length = reference.length();

        return new ClassReference(reference, ResolutionStatus.UNKNOWN, location, lineNumber, column, length,
                    reference);
    }

}
