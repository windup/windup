package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.FileModelService;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectDependency;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.rules.apps.maven.dao.MavenModelService;
import org.jboss.windup.rules.apps.xml.XmlResourceModel;
import org.jboss.windup.util.exception.MarshallingException;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DiscoverMavenProjectsRuleProvider extends WindupRuleProvider
{
    private static final Logger LOG = LoggerFactory.getLogger(DiscoverMavenProjectsRuleProvider.class);

    private static final Map<String, String> namespaces = new HashMap<>();
    static
    {
        namespaces.put("pom", "http://maven.apache.org/POM/4.0.0");
    }

    @Inject
    private MavenModelService mavenModelService;
    @Inject
    private FileModelService fileModelService;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getClassDependencies()
    {
        return generateDependencies(DiscoverXmlFilesRuleProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext arg0)
    {
        ConditionBuilder fileWhen = Query
                    .find(XmlResourceModel.class)
                    .withProperty(FileModel.PROPERTY_FILE_NAME, "pom.xml");

        AbstractIterationOperation<XmlResourceModel> evaluatePomFiles = new AbstractIterationOperation<XmlResourceModel>(
                    XmlResourceModel.class)
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, XmlResourceModel payload)
            {
                GraphContext graphContext = event.getGraphContext();
                MavenProjectModel mavenProjectModel = extractMavenProjectModel(graphContext, payload);
                if (mavenProjectModel != null)
                {
                    ArchiveModel archiveModel = payload.getParentArchive();
                    if (archiveModel != null)
                    {
                        archiveModel.setProjectModel(mavenProjectModel);

                        mavenProjectModel.setRootFileModel(archiveModel);

                        // Attach the project to all files within the archive
                        for (FileModel f : archiveModel.getContainedFileModels())
                        {
                            if (f.getProjectModel() == null)
                            {
                                // only set it if it has not already been set
                                f.setProjectModel(mavenProjectModel);
                                mavenProjectModel.addFileModel(f);
                            }
                        }
                    }
                    else
                    {
                        // add the parent file
                        File parentFile = payload.asFile().getParentFile();
                        FileModel parentFileModel = fileModelService.findByPath(parentFile.getAbsolutePath());
                        if (parentFileModel != null)
                        {
                            parentFileModel.setProjectModel(mavenProjectModel);
                            mavenProjectModel.addFileModel(parentFileModel);
                            mavenProjectModel.setRootFileModel(parentFileModel);

                            // now add all child folders that do not contain pom files
                            for (FileModel childFile : parentFileModel.getContainedFiles())
                            {
                                addFilesToModel(mavenProjectModel, childFile);
                            }
                        }
                    }
                }
            }
        };

        return ConfigurationBuilder.begin()
            .addRule()
            .when(fileWhen)
            .perform(
                Iteration.over().perform(evaluatePomFiles).endIteration()
            );
    }

    private void addFilesToModel(MavenProjectModel mavenProjectModel, FileModel fileModel)
    {
        String filePath = fileModel.getFilePath();
        // First, make sure we aren't looking at a separate module (we assume that if a pom.xml is in the folder,
        // it is a separate module)
        for (FileModel childFile : fileModel.getContainedFiles())
        {
            String filename = childFile.getFileName();
            if (filename.equals("pom.xml"))
            {
                // this is a new project (submodule) -- break;
                return;
            }
        }

        fileModel.setProjectModel(mavenProjectModel);
        mavenProjectModel.addFileModel(fileModel);

        // now recursively all files to the project
        for (FileModel childFile : fileModel.getContainedFiles())
        {
            addFilesToModel(mavenProjectModel, childFile);
        }
    }

    public MavenProjectModel extractMavenProjectModel(GraphContext context, XmlResourceModel xmlResourceModel)
    {
        File myFile = xmlResourceModel.asFile();
        Document document = xmlResourceModel.asDocument();

        // modelVersion
        String modelVersion = XmlUtil.xpathExtract(document, "/pom:project/pom:modelVersion", namespaces);
        String name = XmlUtil.xpathExtract(document, "/pom:project/pom:name", namespaces);
        String description = XmlUtil.xpathExtract(document, "/pom:project/pom:description", namespaces);
        String url = XmlUtil.xpathExtract(document, "/pom:project/pom:url", namespaces);

        String groupId = XmlUtil.xpathExtract(document, "/pom:project/pom:groupId", namespaces);
        String artifactId = XmlUtil.xpathExtract(document, "/pom:project/pom:artifactId", namespaces);
        String version = XmlUtil.xpathExtract(document, "/pom:project/pom:version", namespaces);

        String parentGroupId = XmlUtil.xpathExtract(document, "/pom:project/pom:parent/pom:groupId", namespaces);
        String parentArtifactId = XmlUtil.xpathExtract(document, "/pom:project/pom:parent/pom:artifactId",
                    namespaces);
        String parentVersion = XmlUtil.xpathExtract(document, "/pom:project/pom:parent/pom:version", namespaces);

        if (StringUtils.isBlank(groupId) && StringUtils.isNotBlank(parentGroupId))
        {
            groupId = parentGroupId;
        }
        if (StringUtils.isBlank(version) && StringUtils.isNotBlank(parentVersion))
        {
            version = parentVersion;
        }

        MavenProjectModel mavenProjectModel = mavenModelService.findByGroupArtifactVersion(groupId, artifactId,
                    version);
        if (mavenProjectModel == null)
        {
            mavenProjectModel = mavenModelService.createMavenStub(groupId, artifactId, version);
            mavenProjectModel.addMavenPom(xmlResourceModel);
        }
        else
        {
            // make sure we are associated as a file that provides this maven project information
            boolean found = false;
            for (XmlResourceModel foundPom : mavenProjectModel.getMavenPom())
            {
                File foundPomFile = foundPom.asFile();
                if (foundPomFile.getAbsoluteFile().equals(myFile))
                {
                    // this one is already there
                    found = true;
                    break;
                }
            }

            // if this mavenprojectmodel isn't already associated with a pom file, add it now
            if (!found)
            {
                mavenProjectModel.addMavenPom(xmlResourceModel);
            }
        }

        mavenProjectModel.setName(getReadableNameForProject(name, groupId, artifactId, version));

        if (StringUtils.isNotBlank(description))
        {
            mavenProjectModel.setDescription(StringUtils.trim(description));
        }
        if (StringUtils.isNotBlank(url))
        {
            mavenProjectModel.setURL(StringUtils.trim(url));
        }
        if (StringUtils.isNotBlank(modelVersion))
        {
            mavenProjectModel.setSpecificationVersion(modelVersion);
        }

        if (StringUtils.isNotBlank(parentGroupId))
        {
            // parent
            parentGroupId = resolveProperty(document, namespaces, parentGroupId, version);
            parentArtifactId = resolveProperty(document, namespaces, parentArtifactId, version);
            parentVersion = resolveProperty(document, namespaces, parentVersion, version);

            MavenProjectModel parent = mavenModelService.findByGroupArtifactVersion(parentGroupId,
                        parentArtifactId, parentVersion);
            if (parent == null)
            {
                parent = mavenModelService.createMavenStub(parentGroupId, parentArtifactId, parentVersion);
                parent.setName(getReadableNameForProject(null, parentGroupId, parentArtifactId,
                            parentVersion));
            }

            mavenProjectModel.setParentMavenPOM(parent);
        }

        NodeList nodes = XmlUtil
                    .xpathNodeList(document, "/pom:project/pom:dependencies/pom:dependency", namespaces);
        for (int i = 0, j = nodes.getLength(); i < j; i++)
        {
            Node node = nodes.item(i);
            String dependencyGroupId = XmlUtil.xpathExtract(node, "./pom:groupId", namespaces);
            String dependencyArtifactId = XmlUtil.xpathExtract(node, "./pom:artifactId", namespaces);
            String dependencyVersion = XmlUtil.xpathExtract(node, "./pom:version", namespaces);

            String dependencyClassifier = XmlUtil.xpathExtract(node, "./pom:classifier", namespaces);
            String dependencyScope = XmlUtil.xpathExtract(node, "./pom:scope", namespaces);
            String dependencyType = XmlUtil.xpathExtract(node, "./pom:type", namespaces);

            dependencyGroupId = resolveProperty(document, namespaces, dependencyGroupId, version);
            dependencyArtifactId = resolveProperty(document, namespaces, dependencyArtifactId, version);
            dependencyVersion = resolveProperty(document, namespaces, dependencyVersion, version);

            if (StringUtils.isNotBlank(dependencyGroupId))
            {
                MavenProjectModel dependency = mavenModelService.findByGroupArtifactVersion(dependencyGroupId,
                            dependencyArtifactId, dependencyVersion);
                if (dependency == null)
                {
                    dependency = mavenModelService.createMavenStub(dependencyGroupId, dependencyArtifactId,
                                dependencyVersion);
                    dependency.setName(getReadableNameForProject(null, dependencyGroupId, dependencyArtifactId,
                                dependencyVersion));
                }
                ProjectDependency projectDep = context.getFramed().addVertex(null, ProjectDependency.class);
                projectDep.setClassifier(dependencyClassifier);
                projectDep.setScope(dependencyScope);
                projectDep.setType(dependencyType);
                projectDep.setProject(dependency);
                mavenProjectModel.addDependency(projectDep);
            }
        }
        return mavenProjectModel;
    }

    private String getReadableNameForProject(String mavenName, String groupId, String artifactId, String version)
    {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(mavenName))
        {
            sb.append(mavenName);
            sb.append(" (");
        }

        sb.append(groupId).append(":").append(artifactId).append(":").append(version);

        if (StringUtils.isNotBlank(mavenName))
        {
            sb.append(")");
        }

        return sb.toString();
    }

    private String resolveProperty(Document document, Map<String, String> namespaces, String property,
                String projectVersion) throws MarshallingException
    {
        if (StringUtils.startsWith(property, "${"))
        {
            // is property...
            String propertyName = StringUtils.removeStart(property, "${");
            propertyName = StringUtils.removeEnd(propertyName, "}");

            switch (propertyName)
            {
            case "pom.version":
            case "project.version":
                return projectVersion;
            default:
                NodeList nodes = XmlUtil.xpathNodeList(document, "//pom:properties/pom:" + propertyName, namespaces);

                if (nodes.getLength() == 0 || nodes.item(0) == null)
                {
                    LOG.warn("Expected: " + property + " but it wasn't found in the POM.");
                }
                else
                {
                    Node node = nodes.item(0);
                    String value = node.getTextContent();
                    return value;
                }
            }

        }
        return property;
    }
}
