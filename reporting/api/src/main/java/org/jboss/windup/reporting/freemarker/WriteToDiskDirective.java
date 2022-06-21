package org.jboss.windup.reporting.freemarker;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.exception.WindupException;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * Writes the contents of the directive to disk in the reports data directory.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class WriteToDiskDirective implements WindupFreeMarkerTemplateDirective {
    public static final String NAME = "write_to_disk";
    public static final String FILENAME = "filename";
    private GraphContext context;

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        SimpleScalar filename = (SimpleScalar) params.get(FILENAME);
        if (filename == null || StringUtils.isBlank(filename.getAsString()))
            throw new WindupException(NAME + " - Validation error, " + FILENAME + " parameter must not be blank!");

        Path dataDirectory = new ReportService(context).getReportDataDirectory();
        Path outputPath = dataDirectory.resolve(filename.getAsString());

        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            body.render(writer);
        }
    }

    @Override
    public String getDirectiveName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Writes the contents of the directive to disk";
    }

    @Override
    public void setContext(GraphRewrite event) {
        this.context = event.getGraphContext();
    }
}
