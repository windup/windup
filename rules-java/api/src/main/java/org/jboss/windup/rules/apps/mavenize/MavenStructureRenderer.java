package org.jboss.windup.rules.apps.mavenize;

import freemarker.core.ParseException;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.windup.reporting.freemarker.FurnaceFreeMarkerTemplateLoader;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;

/**
 * Recursively renders the previously created Maven project structure into pom.xml's in a directory tree.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, ozizka at seznam.cz</a>
 */
public class MavenStructureRenderer
{
    private static final Logger LOG = Logging.get(MavenStructureRenderer.class);
    private static final String TEMPLATE_POM_XML = "/org/jboss/windup/rules/apps/mavenize/pom.xml.ftl";
    private static final String TEMPLATE_BOM_XML = "/org/jboss/windup/rules/apps/mavenize/bom.xml.ftl";

    private MavenizationService.MavenizationContext mavCtx;

    MavenStructureRenderer(MavenizationService.MavenizationContext mavCtx)
    {
        this.mavCtx = mavCtx;
    }


    void createMavenProjectDirectoryTree()
    {
        try {
            // Root POM
            renderPomXml(mavCtx, mavCtx.getRootPom(), mavCtx.getMavenizedBaseDir().resolve("pom.xml"));

            List<Throwable> exceptions = new ArrayList<>();
            for (Map.Entry<String, Pom> entry : mavCtx.getRootPom().submodules.entrySet())
            {
                try {
                    String subDir = entry.getKey();
                    Path resultPomXmlPath = mavCtx.getMavenizedBaseDir().resolve(subDir).resolve("pom.xml");
                    LOG.info("Writing " + subDir + "/pom.xml" + "\n > " + entry.getValue());
                    renderPomXml(mavCtx, entry.getValue(), resultPomXmlPath);
                }
                catch (Throwable ex)
                {
                    exceptions.add(ex);
                }
            }
            throwIfErrors(exceptions);
        }
        catch (Exception ex) {
            throw new WindupException("Failed creating the Maven project structure: " + ex.getMessage(), ex);
        }
    }


    private static void renderPomXml(MavenizationService.MavenizationContext mavCtx, Pom pom, Path pomXmlPath)
    {
        Map vars = new HashMap();
        vars.put("pom", pom);

        Path template = chooseTemplate(pom);

        try
        {
            LOG.info("Rendering template: " + template + " into " + pomXmlPath + "\n - " + pom);
            Files.createDirectories(pomXmlPath.getParent());
            renderFreemarkerTemplate(template, vars, pomXmlPath);
        }
        catch (ParseException ex)
        {
            throw new WindupException("Could not parse pom.xml template: " + template + "\nReason: " + ex.getMessage(), ex);
        }
        catch (IOException | TemplateException ex)
        {
            throw new WindupException("Error rendering pom.xml template: " + template + "\nReason: " + ex.getMessage(), ex);
        }
    }


    private static Path chooseTemplate(Pom pom)
    {
        Path template;
        switch(pom.role) {
            case BOM: template = Paths.get(TEMPLATE_BOM_XML); break;
            default:
                switch(pom.coord.getPackaging()){
                    case "pom":
                    case "jar":
                    case "war":
                    case "ear":
                    default: template = Paths.get(TEMPLATE_POM_XML); break;
                    case "bom": template = Paths.get(TEMPLATE_BOM_XML); break; // Not really used, not a standard Maven packaging.
                }
                break;
        }
        return template;
    }

    /**
     * Renders the given FreeMarker template to given directory, using given variables.
     */
    private static void renderFreemarkerTemplate(Path templatePath, Map vars, Path outputPath)
            throws MalformedTemplateNameException, ParseException, IOException, TemplateException
    {
        if(templatePath == null)
            throw new WindupException("templatePath is null");

        freemarker.template.Configuration freemarkerConfig = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_23);
        DefaultObjectWrapperBuilder objectWrapperBuilder = new DefaultObjectWrapperBuilder(freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        objectWrapperBuilder.setUseAdaptersForContainers(true);
        freemarkerConfig.setObjectWrapper(objectWrapperBuilder.build());
        //Map<String, Object> objects = new HashMap<>();

        freemarkerConfig.setTemplateLoader(new FurnaceFreeMarkerTemplateLoader());
        Template template = freemarkerConfig.getTemplate(templatePath.toString());
        try (FileWriter fw = new FileWriter(outputPath.toFile()))
        {
            template.process(vars, fw);
        }
    }



    private void throwIfErrors(List<Throwable> exceptions) throws WindupException
    {
        if (exceptions.isEmpty())
            return;

        StringBuilder sb = new StringBuilder("Errors when creating the Maven project directory tree:\n");
        for (Throwable ex : exceptions)
        {
            sb.append("    ").append(ex.getMessage()).append("\n");
        }
        sb.append("The first error's stack trace:\n    ");
        sb.append(ExceptionUtils.getStackTrace(exceptions.get(0)));
        throw new WindupException(sb.toString());
    }

}
