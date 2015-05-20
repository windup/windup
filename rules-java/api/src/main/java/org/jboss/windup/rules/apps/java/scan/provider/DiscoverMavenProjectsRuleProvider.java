package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectDependencyModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.rules.apps.java.scan.operation.packagemapping.PackageNameMapping;
import org.jboss.windup.rules.apps.maven.dao.MavenProjectService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.MarshallingException;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Discover Maven pom files and build a {@link MavenProjectModel} containing this metadata.
 */
public class DiscoverMavenProjectsRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logging.get(DiscoverMavenProjectsRuleProvider.class);

    private static final Map<String, String> namespaces = new HashMap<>();

    static
    {
        namespaces.put("pom", "http://maven.apache.org/POM/4.0.0");
    }

    public DiscoverMavenProjectsRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverMavenProjectsRuleProvider.class)
                    .setPhase(DiscoverProjectStructurePhase.class)
                    .setHaltOnException(true));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder fileWhen = Query
                    .fromType(XmlFileModel.class)
                    .withProperty(FileModel.FILE_NAME, "pom.xml");

        final ClassificationService classificationService = new ClassificationService(context);
        final TechnologyTagService technologyTagService = new TechnologyTagService(context);
        
        AbstractIterationOperation<XmlFileModel> evaluatePomFiles = new AbstractIterationOperation<XmlFileModel>()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
            {
                /*
                 * Make sure we don't add try to create multiple projects out of it
                 */
                if (payload.getProjectModel() != null)
                    return;

                // get a default name from the parent file (if the maven project doesn't contain one)
                String defaultName = payload.getParentArchive() == null ? payload.asFile().getParentFile().getName() : payload.getParentArchive()
                            .getFileName();
                MavenProjectModel mavenProjectModel = extractMavenProjectModel(event, defaultName, payload);
                if (mavenProjectModel != null)
                {
                    //add classification information to file.
                    classificationService.attachClassification(payload, "Maven POM", "Maven Project Object Model (POM) File");
                    technologyTagService.addTagToFileModel(payload, "Maven XML", TechnologyTagLevel.INFORMATIONAL);
                    
                    ArchiveModel archiveModel = payload.getParentArchive();
                    if (archiveModel != null && !isAlreadyMavenProject(archiveModel))
                    {
                        archiveModel.setProjectModel(mavenProjectModel);

                        mavenProjectModel.setRootFileModel(archiveModel);

                        // Attach the project to all files within the archive
                        for (FileModel f : archiveModel.getContainedFileModels())
                        {
                            // don't add archive models, as those really are separate projects...
                            // also, don't set the project model if one is already set
                            if (!(f instanceof ArchiveModel) && f.getProjectModel() == null)
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
                        FileModel parentFileModel = new FileService(event.getGraphContext()).findByPath(parentFile.getAbsolutePath());
                        if (parentFileModel != null && !isAlreadyMavenProject(parentFileModel))
                        {
                            parentFileModel.setProjectModel(mavenProjectModel);
                            mavenProjectModel.addFileModel(parentFileModel);
                            mavenProjectModel.setRootFileModel(parentFileModel);

                            // now add all child folders that do not contain pom files
                            for (FileModel childFile : parentFileModel.getFilesInDirectory())
                            {
                                addFilesToModel(mavenProjectModel, childFile);
                            }
                        }
                    }
                }
            }

            @Override
            public String toString()
            {
                return "ScanMavenProject";
            }
        };

        // @formatter:off
        return ConfigurationBuilder.begin()
                .addRule()
                .when(fileWhen)
                .perform(evaluatePomFiles);
        // @formatter:on
    }

    /**
     * This method is here so that the caller can know not to try to reset the project model for an archive (or directory) if the archive (or
     * directory) is already a maven project.
     * <p/>
     * This can sometimes help in cases in which an archive includes multiple poms in its META-INF.
     */
    private boolean isAlreadyMavenProject(FileModel fileModel)
    {
        return fileModel.getProjectModel() != null && fileModel.getProjectModel() instanceof MavenProjectModel;
    }

    private void addFilesToModel(MavenProjectModel mavenProjectModel, FileModel fileModel)
    {
        // First, make sure we aren't looking at a separate module (we assume that if a pom.xml is in the folder,
        // it is a separate module)
        for (FileModel childFile : fileModel.getFilesInDirectory())
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
        for (FileModel childFile : fileModel.getFilesInDirectory())
        {
            addFilesToModel(mavenProjectModel, childFile);
        }
    }

    public MavenProjectModel extractMavenProjectModel(GraphRewrite event, String defaultProjectName, XmlFileModel xmlFileModel)
    {
        File xmlFile = xmlFileModel.asFile();
        Document document = new XmlFileService(event.getGraphContext()).loadDocumentQuiet(xmlFileModel);
        if (document == null)
        {
            LOG.warning("Could not parse pom at: " + xmlFileModel.getFilePath() + " skipping maven project discovery for this.");
            return null;
        }

        // modelVersion
        String modelVersion = XmlUtil.xpathExtract(document, "/pom:project/pom:modelVersion", namespaces);
        String name = XmlUtil.xpathExtract(document, "/pom:project/pom:name", namespaces);
        String organization = XmlUtil.xpathExtract(document, "/pom:project/pom:organization", namespaces);
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

        if (StringUtils.isBlank(organization))
        {
            organization = PackageNameMapping.getOrganizationForPackage(event, groupId);
        }

        MavenProjectService mavenProjectService = new MavenProjectService(event.getGraphContext());
        MavenProjectModel mavenProjectModel = getMavenStubProject(mavenProjectService, groupId, artifactId, version);
        /*
         * We don't want to reuse one that is already associated with a file (defined twice). This happens sometimes if the same maven gav is defined
         * multiple times within the input application.
         */
        if (mavenProjectModel == null)
        {
            LOG.info("Creating maven project for pom at: " + xmlFileModel.getFilePath() + " with gav: " + groupId + "," + artifactId + ","
                        + version);
            mavenProjectModel = mavenProjectService.createMavenStub(groupId, artifactId, version);
            mavenProjectModel.addMavenPom(xmlFileModel);
        }
        else
        {
            // make sure we are associated as a file that provides this maven project information
            boolean found = false;
            for (XmlFileModel foundPom : mavenProjectModel.getMavenPom())
            {
                File foundPomFile = foundPom.asFile();
                if (foundPomFile.getAbsoluteFile().equals(xmlFile))
                {
                    // this one is already there
                    found = true;
                    break;
                }
            }

            // if this mavenprojectmodel isn't already associated with a pom file, add it now
            if (!found)
            {
                mavenProjectModel.addMavenPom(xmlFileModel);
            }
        }

        if (StringUtils.isBlank(name))
        {
            name = defaultProjectName;
        }
        mavenProjectModel.setName(getReadableNameForProject(name, groupId, artifactId, version));

        if (StringUtils.isNotBlank(organization))
        {
            mavenProjectModel.setOrganization(organization);
        }
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

            MavenProjectModel parent = getMavenProject(mavenProjectService, parentGroupId, parentArtifactId, parentVersion);

            if (parent == null)
            {
                parent = mavenProjectService.createMavenStub(parentGroupId, parentArtifactId, parentVersion);
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
                MavenProjectModel dependency = getMavenProject(mavenProjectService, dependencyGroupId, dependencyArtifactId, dependencyVersion);
                if (dependency == null)
                {
                    dependency = mavenProjectService.createMavenStub(dependencyGroupId, dependencyArtifactId,
                                dependencyVersion);
                    dependency.setName(getReadableNameForProject(null, dependencyGroupId, dependencyArtifactId,
                                dependencyVersion));
                }
                ProjectDependencyModel projectDep = new GraphService<>(event.getGraphContext(), ProjectDependencyModel.class).create();
                projectDep.setClassifier(dependencyClassifier);
                projectDep.setScope(dependencyScope);
                projectDep.setType(dependencyType);
                projectDep.setProject(dependency);
                mavenProjectModel.addDependency(projectDep);
            }
        }
        return mavenProjectModel;
    }

    /**
     * This will return a {@link MavenProjectModel} with the give gav, preferring one that has been found in the input application as opposed to a
     * stub.
     */
    private MavenProjectModel getMavenProject(MavenProjectService mavenProjectService, String groupId, String artifactId, String version)
    {
        Iterable<MavenProjectModel> possibleProjects = mavenProjectService.findByGroupArtifactVersion(groupId, artifactId, version);
        MavenProjectModel project = null;
        for (MavenProjectModel possibleProject : possibleProjects)
        {
            if (possibleProject.getRootFileModel() != null)
            {
                return possibleProject;
            }
            else if (project == null)
            {
                project = possibleProject;
            }
        }
        return project;
    }

    /**
     * A Maven stub is a Maven Project for which we have found information, but the project has not yet been located within the input application. If
     * we have found an application of the same GAV within the input app, we should fill out this stub instead of creating a new one.
     */
    private MavenProjectModel getMavenStubProject(MavenProjectService mavenProjectService, String groupId, String artifactId, String version)
    {
        Iterable<MavenProjectModel> mavenProjectModels = mavenProjectService.findByGroupArtifactVersion(groupId, artifactId, version);
        if (!mavenProjectModels.iterator().hasNext())
        {
            return null;
        }
        for (MavenProjectModel mavenProjectModel : mavenProjectModels)
        {
            if (mavenProjectModel.getRootFileModel() == null)
            {
                // this is a stub... we can fill it in with details
                return mavenProjectModel;
            }
        }
        return null;
    }

    private String getReadableNameForProject(String mavenName, String groupId, String artifactId, String version)
    {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(mavenName))
        {
            sb.append(mavenName);
            sb.append(" (");
        }

        if (StringUtils.isNotBlank(groupId) || StringUtils.isNotBlank(artifactId) || StringUtils.isNotBlank(version))
        {
            sb.append(groupId).append(":").append(artifactId).append(":").append(version);
        }

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
                    LOG.warning("Expected: " + property + " but it wasn't found in the POM.");
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
