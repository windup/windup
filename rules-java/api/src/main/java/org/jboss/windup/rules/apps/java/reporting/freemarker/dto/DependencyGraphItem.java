package org.jboss.windup.rules.apps.java.reporting.freemarker.dto;

import org.apache.commons.io.FilenameUtils;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.dependencyreport.DependencyReportDependencyGroupModel;

import java.util.HashMap;
import java.util.Map;

/**
 * A data transfer object carying information about the dependency
 *
 * @author <a href="mailto:marcorizzi82@gmail.com>Marco Rizzi</a>
 */
public class DependencyGraphItem {
    private final Kind kind;
    private final Map<String, String> metadata;

    public DependencyGraphItem(DependencyReportDependencyGroupModel dependencyReportDependencyGroupModel) {
        this(dependencyReportDependencyGroupModel.getCanonicalProject());
    }

    public DependencyGraphItem(ProjectModel projectModel) {
        this.metadata = new HashMap<>(1);
        this.metadata.put("name", projectModel.getRootFileModel().getFileName());
        this.kind = Kind.getKind(projectModel);
    }

    public String getKind() {
        return kind.getValue();
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    enum Kind {

        EAR("Ear"), WAR_APP("WarApp"), JAR_APP("JarApp"), WAR("War"), JAR("Jar"), EXTERNAL_JAR("ExternalJar"), UNKNOWN("unknown");

        private String value;

        Kind(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        static Kind getKind(ProjectModel projectModel) {
            String projectType;
            if (projectModel.getProjectType() == null) {
                if (!projectModel.getRootFileModel().isDirectory()) {
                    projectType = FilenameUtils.getExtension(projectModel.getRootFileModel().getFileName());
                }
                // if we're analyzing an exploded app we have a directory
                else {
                    projectType = "war";
                }
            } else {
                projectType = projectModel.getProjectType();
            }
            boolean isChildren = projectModel.getParentProject() != null;
            boolean isSkipped = projectModel.getRootFileModel() instanceof IdentifiedArchiveModel;
            switch (projectType.toLowerCase()) {
                case "jar":
            		return isChildren ? (isSkipped ? EXTERNAL_JAR : JAR) : JAR_APP;
                case "war":
                    return isChildren ? WAR : WAR_APP;
                case "ear":
                    return EAR;
                default:
                    return UNKNOWN; // should never happen, but who knows...
            }
        }

    }
}
