package org.jboss.windup.rules.files;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.GraphRule;
import org.jboss.windup.config.PreRulesetEvaluation;
import org.jboss.windup.config.phase.ArchiveMetadataExtractionPhase;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.FileModel.OnParseError;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Maps file extensions to {@link WindupVertexFrame} types. Mappings are always applied during the
 * {@link ArchiveMetadataExtractionPhase} phase, no matter where this rule appears in the pipeline. The following example
 * demonstrates how to match files ending with *.xml to a frame type:
 *
 * <pre>
 *   {@link FileMapping}.from(".*\\.xml").to({@link XmlFileModel}.class)
 * </pre>
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class FileMapping extends GraphRule implements PreRulesetEvaluation, FileMappingFrom, FileMappingTo, FileMappingWithID {
    private static final Logger LOG = Logging.get(FileMapping.class);


    private final Pattern pattern;
    private final List<Class<? extends WindupVertexFrame>> types = new ArrayList<>();
    private OnParseError onParseError = OnParseError.WARN;

    private String id;

    private FileMapping(Pattern pattern) {
        this.pattern = pattern;

        String normalizedPattern = StringUtils.replacePattern(pattern.pattern(), "\\s", "_");
        normalizedPattern = StringUtils.substring(normalizedPattern, 0, 10);
        this.id = this.getClass().getSimpleName() + "_" + normalizedPattern
                + "_" + RandomStringUtils.randomAlphanumeric(2);
    }

    /**
     * Create a new {@link FileMapping}
     */
    public static FileMappingFrom from(String regex) {
        return new FileMapping(Pattern.compile(regex));
    }

    /**
     * Get the list of pattern to {@link WindupVertexFrame} type mappings for the {@link GraphRewrite} event.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<Class<? extends WindupVertexFrame>>> getMappings(GraphRewrite event) {
        Map<String, List<Class<? extends WindupVertexFrame>>> map = (Map<String, List<Class<? extends WindupVertexFrame>>>) event
                .getRewriteContext().get(FileMapping.class);
        if (map == null) {
            map = new HashMap<>();
            event.getRewriteContext().put(FileMapping.class, map);
        }
        return map;
    }

    /**
     * Get the list of {@link WindupVertexFrame} type mappings for the given pattern and {@link GraphRewrite} event.
     */
    public static List<Class<? extends WindupVertexFrame>> getMappings(GraphRewrite event, String pattern) {
        Map<String, List<Class<? extends WindupVertexFrame>>> mappings = getMappings(event);
        List<Class<? extends WindupVertexFrame>> result = mappings.get(pattern);
        if (result == null) {
            result = new ArrayList<>();
            mappings.put(pattern, result);
        }
        return result;
    }

    /**
     * Add a {@link WindupVertexFrame} type to the list of mappings for the given pattern and {@link GraphRewrite}
     * event.
     */
    public static void addMapping(GraphRewrite event, String pattern, Class<? extends WindupVertexFrame> type) {
        getMappings(event, pattern).add(type);
    }

    @Override
    public FileMappingWithID withId(String ruleID) {
        this.id = ruleID;
        return this;
    }

    @Override
    public FileMappingTo to(Class<? extends WindupVertexFrame> type) {
        Assert.notNull(type, "Model type must not be null.");
        this.types.add(type);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FileMappingTo to(Class<? extends WindupVertexFrame>... types) {
        Assert.notNull(types, "Model type list must not be null.");
        this.types.addAll(Arrays.asList(types));
        return this;
    }

    @Override
    public FileMappingTo onParseError(OnParseError onParseError) {
        this.onParseError = onParseError;
        return this;
    }

    @Override
    public void preRulesetEvaluation(GraphRewrite event) {
        LOG.info("Added " + toString());
        for (Class<? extends WindupVertexFrame> type : types) {
            addMapping(event, pattern.pattern(), type);
        }

        /*
         * Handle mapping any files that were directly inserted in the graph before this executed.
         */
        Map<String, List<Class<? extends WindupVertexFrame>>> mappings = FileMapping
                .getMappings(event);

        FileService fileService = new FileService(event.getGraphContext());
        for (Entry<String, List<Class<? extends WindupVertexFrame>>> entry : mappings.entrySet()) {
            String pattern = entry.getKey();
            List<Class<? extends WindupVertexFrame>> types = entry.getValue();

            Iterable<FileModel> models = fileService.findAllByPropertyMatchingRegex(FileModel.FILE_PATH, pattern);

            for (FileModel model : models) {
                if (model.isDirectory())
                    continue;

                for (Class<? extends WindupVertexFrame> type : types)
                    GraphService.addTypeToModel(event.getGraphContext(), model, type);

                if (this.onParseError == OnParseError.IGNORE)
                    model.setOnParseError(OnParseError.IGNORE);

                LOG.info("Mapped file [" + model.getFilePath() + "] matching pattern [" + pattern + "] to the following [" + types.size()
                        + "] types: " + types);
            }
        }
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context) {
        return true;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context) {
        // No-op
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName());
        builder.append(".from(").append(pattern).append(")");
        builder.append(".to(").append(types).append(")");
        return builder.toString();
    }
}
