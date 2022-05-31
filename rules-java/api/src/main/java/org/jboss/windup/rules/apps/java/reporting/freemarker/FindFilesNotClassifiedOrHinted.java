package org.jboss.windup.rules.apps.java.reporting.freemarker;

import freemarker.template.TemplateModelException;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.freemarker.FreeMarkerUtil;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.query.FindFilesNotClassifiedOrHintedGremlinCriterion;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.util.ExecutionStatistics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Finds the files that have not had {@link ClassificationModel}s linked, and also does not have {@link FileLocationModel}s linked.
 * <p>
 * Called by:
 * <p>
 * findFilesNotClassifiedOrHinted(Iterable<FileModel>)
 * <p>
 * NOTE: This will only return JavaSourceFileModels and XmlFileModels in order to reduce clutter.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FindFilesNotClassifiedOrHinted implements WindupFreeMarkerMethod {
    private static final String NAME = "findFilesNotClassifiedOrHinted";
    private GraphContext context;

    @Override
    public void setContext(GraphRewrite event) {
        this.context = event.getGraphContext();
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1) {
            throw new TemplateModelException("Error, method expects one argument (Iterable<FileModel>)");
        }
        @SuppressWarnings("unchecked")
        Iterable<FileModel> fileModels = FreeMarkerUtil.freemarkerWrapperToIterable(arguments.get(0));

        FindFilesNotClassifiedOrHintedGremlinCriterion criterion = new FindFilesNotClassifiedOrHintedGremlinCriterion();
        List<Vertex> initialFileModelsAsVertices = new ArrayList<>();
        for (FileModel fm : fileModels) {
            initialFileModelsAsVertices.add(fm.getElement());
        }
        Iterable<Vertex> result = criterion.query(context, initialFileModelsAsVertices);

        List<FileModel> resultModels = new ArrayList<>();
        for (Vertex v : result) {
            FileModel f = context.getFramed().frameElement(v, FileModel.class);

            //we don't want to show our decompiled classes in the report
            boolean wasNotGenerated = f.isWindupGenerated() == null || !f.isWindupGenerated();
            boolean isOfInterestingType = f instanceof JavaSourceFileModel || f instanceof XmlFileModel || f instanceof JavaClassFileModel;
            //we don't want to list .class files that have their decompiled .java file with hints/classifications
            boolean withoutHiddenHints = true;

            if (f instanceof JavaClassFileModel) {
                Iterator<Vertex> decompiled = v.vertices(Direction.OUT, JavaClassFileModel.DECOMPILED_FILE);
                if (decompiled.hasNext()) {
                    withoutHiddenHints = !decompiled.next().vertices(Direction.IN, FileReferenceModel.FILE_MODEL).hasNext();
                }
            }

            if (wasNotGenerated && withoutHiddenHints && isOfInterestingType) {
                //if it passed all the checks, add it
                resultModels.add(f);
            }
        }

        ExecutionStatistics.get().end(NAME);
        return resultModels;
    }

    @Override
    public String getDescription() {
        return "Takes an Iterable<" + FileModel.class.getSimpleName()
                + "> as a parameter and returns the files that have neither " + ClassificationModel.class.getSimpleName()
                + "s nor " + InlineHintModel.class.getSimpleName() + "s associated with them.";
    }
}
