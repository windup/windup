package org.jboss.windup.rules.apps.java.scan.provider;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.DependencyLocation;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.ProjectDependencyModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.graph.model.IgnoredArchiveModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.rules.apps.java.scan.operation.packagemapping.PackageNameMapping;
import org.jboss.windup.rules.apps.maven.dao.MavenProjectService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.MarshallingException;
import org.jboss.windup.util.xml.LocationAwareContentHandler;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Discover Maven pom files and build a {@link MavenProjectModel} containing this metadata.
 */
@RuleMetadata(phase = DiscoverProjectStructurePhase.class, haltOnException = true)
public class DiscoverMavenProjectsRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logging.get(DiscoverMavenProjectsRuleProvider.class);
    private static final Map<String, String> namespaces = new HashMap<>();

    private final MavenElementValueResolver mavenElementValueResolver = new MavenElementValueResolver();

    static
    {
        namespaces.put("pom", "http://maven.apache.org/POM/4.0.0");
    }

    private final MavenProjectDependencyLocationExtractor mavenProjectDependencyLocationExtractor = new MavenProjectDependencyLocationExtractor();

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        ConditionBuilder fileWhen = Query
                    .fromType(XmlFileModel.class)
                    .withProperty(FileModel.FILE_NAME, "pom.xml");

        AbstractIterationOperation<XmlFileModel> evaluatePomFiles = new AbstractIterationOperation<>()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
            {
                /*
                 * Make sure we don't add try to create multiple projects out of it
                 */
                if (payload.getProjectModel() != null)
                    return;

                final ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                final TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());

                // get a default name from the parent file (if the maven project doesn't contain one)
                String defaultName = payload.getArchive() == null ? payload.asFile().getParentFile().getName() : payload.getArchive()
                            .getFileName();
                MavenProjectModel mavenProjectModel = extractMavenProjectModel(event, context, defaultName, payload);
                if (mavenProjectModel != null)
                {
                    // Do not classify POM files if they are part of an ignored archive, to keep them from appearing in the Issues report
                    if (!(payload.getArchive() instanceof IgnoredArchiveModel)) {
                        LOG.log(Level.INFO, "Entering ignored");
                        classificationService.attachClassification(event, context, payload, IssueCategoryRegistry.INFORMATION, "Maven POM (pom.xml)", "Maven Project Object Model (POM) File");
                        technologyTagService.addTagToFileModel(payload, "Maven XML", TechnologyTagLevel.INFORMATIONAL);
                    }

                    ArchiveModel archiveModel = payload.getArchive();
                    if (archiveModel != null && !isAlreadyMavenProject(archiveModel))
                    {
                        mavenProjectModel.addFileModel(archiveModel);
                        mavenProjectModel.setRootFileModel(archiveModel);

                        // Attach the project to all files within the archive
                        for (FileModel f : archiveModel.getAllFiles())
                        {
                            // don't add archive models, as those really are separate projects...
                            // also, don't set the project model if one is already set
                            if (!(f instanceof ArchiveModel) && f.getProjectModel() == null)
                            {
                                // only set it if it has not already been set
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
     * This method is here so that the caller can know not to try to reset the project model for an archive (or
     * directory) if the archive (or directory) is already a maven project.
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

        // Also, make sure it isn't a zip file with a pom (that also couldn't be part of the same project)
        if (fileModel instanceof ArchiveModel)
        {
            ArchiveModel childArchive = (ArchiveModel)fileModel;
            if (childArchive instanceof DuplicateArchiveModel)
                childArchive = ((DuplicateArchiveModel) childArchive).getCanonicalArchive();

            // If we separately identified this as a known module, don't override its project settings
            if (childArchive instanceof IgnoredArchiveModel || childArchive instanceof IdentifiedArchiveModel)
                return;

            for (FileModel archiveChild : childArchive.getAllFiles())
            {
                String filename = archiveChild.getFileName();
                if (filename.equals("pom.xml"))
                {
                    // this is a new project (submodule) -- break;
                    return;
                }
            }
        }

        mavenProjectModel.addFileModel(fileModel);

        // now recursively all files to the project
        for (FileModel childFile : fileModel.getFilesInDirectory())
        {
            addFilesToModel(mavenProjectModel, childFile);
        }
    }

    public MavenProjectModel extractMavenProjectModel(GraphRewrite event, EvaluationContext context, String defaultProjectName,
                XmlFileModel xmlFileModel)
    {
        Document document;
        try
        {
            document = new XmlFileService(event.getGraphContext()).loadDocument(event, context, xmlFileModel);
        }
        catch (Exception ex)
        {
            xmlFileModel.setParseError("Could not parse POM XML: " + ex.getMessage());
            LOG.warning("Could not parse POM XML for '" + xmlFileModel.getFilePath()
                    + "':"+System.lineSeparator()+"\t" + ex.getMessage() + System.lineSeparator()+" \tSkipping Maven project discovery.");
            return null;
        }

        String modelVersion = XmlUtil.xpathExtract(document, "/pom:project/pom:modelVersion | /project/modelVersion", namespaces);
        String name = XmlUtil.xpathExtract(document, "/pom:project/pom:name | /project/name", namespaces);
        String organization = XmlUtil.xpathExtract(document, "/pom:project/pom:organization | /project/organization", namespaces);
        String description = XmlUtil.xpathExtract(document, "/pom:project/pom:description | /project/description", namespaces);
        String url = XmlUtil.xpathExtract(document, "/pom:project/pom:url | /project/url", namespaces);

        String groupId = XmlUtil.xpathExtract(document, "/pom:project/pom:groupId | /project/groupId", namespaces);
        String artifactId = XmlUtil.xpathExtract(document, "/pom:project/pom:artifactId | /project/artifactId", namespaces);
        String version = XmlUtil.xpathExtract(document, "/pom:project/pom:version | /project/version", namespaces);

        String parentGroupId = XmlUtil.xpathExtract(document, "/pom:project/pom:parent/pom:groupId | /project/parent/groupId", namespaces);
        String parentArtifactId = XmlUtil.xpathExtract(document, "/pom:project/pom:parent/pom:artifactId | /project/parent/artifactId", namespaces);
        String parentVersion = XmlUtil.xpathExtract(document, "/pom:project/pom:parent/pom:version | /project/parent/version", namespaces);

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
            organization = PackageNameMapping.getOrganizationFromMappings(event, groupId);
        }

        MavenProjectService mavenProjectService = new MavenProjectService(event.getGraphContext());
        MavenProjectModel mavenProjectModel = getMavenStubProject(mavenProjectService, groupId, artifactId, version);
        /*
         * We don't want to reuse one that is already associated with a file (defined twice). This happens sometimes if
         * the same maven gav is defined multiple times within the input application.
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
                File pomXmlFile = xmlFileModel.asFile();
                if (foundPomFile.getAbsoluteFile().equals(pomXmlFile))
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
            parentGroupId = mavenElementValueResolver.resolveValue(document, namespaces, parentGroupId, version);
            parentArtifactId = mavenElementValueResolver.resolveValue(document, namespaces, parentArtifactId, version);
            parentVersion = mavenElementValueResolver.resolveValue(document, namespaces, parentVersion, version);

            MavenProjectModel parent = getMavenProject(mavenProjectService, parentGroupId, parentArtifactId, parentVersion);

            if (parent == null)
            {
                parent = mavenProjectService.createMavenStub(parentGroupId, parentArtifactId, parentVersion);
                parent.setName(getReadableNameForProject(null, parentGroupId, parentArtifactId,
                            parentVersion));
            }

            mavenProjectModel.setParentMavenPOM(parent);
        }

        NodeList nodes = XmlUtil.xpathNodeList(document, getDependenciesXpath(), namespaces);
        for (int i = 0, j = nodes.getLength(); i < j; i++)
        {
            Node node = nodes.item(i);
            String dependencyGroupId = XmlUtil.xpathExtract(node, "./pom:groupId | ./groupId", namespaces);
            String dependencyArtifactId = XmlUtil.xpathExtract(node, "./pom:artifactId | ./artifactId", namespaces);
            String dependencyVersion = XmlUtil.xpathExtract(node, "./pom:version | ./version", namespaces);

            String dependencyClassifier = XmlUtil.xpathExtract(node, "./pom:classifier | ./classifier", namespaces);
            String dependencyScope = XmlUtil.xpathExtract(node, "./pom:scope | ./scope", namespaces);
            String dependencyType = XmlUtil.xpathExtract(node, "./pom:type | ./type", namespaces);
            String dependencyOptional = XmlUtil.xpathExtract(node, "./pom:optional | ./optional", namespaces);

            dependencyGroupId = mavenElementValueResolver.resolveValue(document, namespaces, dependencyGroupId, version);
            dependencyArtifactId = mavenElementValueResolver.resolveValue(document, namespaces, dependencyArtifactId, version);
            dependencyVersion = mavenElementValueResolver.resolveValue(document, namespaces, dependencyVersion, version);

            // optional check introduced with WINDUP-2771
            if (StringUtils.isNotBlank(dependencyGroupId) && !Boolean.parseBoolean(dependencyOptional))
            {
                MavenProjectModel dependency = getMavenProject(mavenProjectService, dependencyGroupId, dependencyArtifactId, dependencyVersion);
                if (dependency == null)
                {
                    dependency = mavenProjectService.createMavenStub(dependencyGroupId, dependencyArtifactId,
                                dependencyVersion);
                    dependency.setName(getReadableNameForProject(null, dependencyGroupId, dependencyArtifactId,
                                dependencyVersion));
                }

                DependencyLocation dependencyLocation = mavenProjectDependencyLocationExtractor.extractDependencyLocation(node);

                ProjectDependencyModel projectDep = new GraphService<>(event.getGraphContext(), ProjectDependencyModel.class).create();
                projectDep.setClassifier(dependencyClassifier);
                projectDep.setScope(dependencyScope);
                projectDep.setType(dependencyType);
                projectDep.setProject(dependency);
                projectDep.setDependencyLocation(dependencyLocation);

                List<FileLocationModel> fileLocationList = extractFileLocations(event, xmlFileModel, node);
                projectDep.setFileLocationReference(fileLocationList);
                mavenProjectModel.addDependency(projectDep);
            }
        }
        return mavenProjectModel;
    }

    private String getDependenciesXpath() {
        String parent               = "/pom:project/pom:parent | /project/parent";
        String dependencies         = "/pom:project/pom:dependencies/pom:dependency  | /project/dependencies/dependency | /pom:project/pom:profiles/pom:profile/pom:dependencies/pom:dependency  | /project/profiles/profile/dependencies/dependency";
        String pluginDependencies   = "/pom:project/pom:build/pom:plugins/pom:plugin | /project/build/plugins/plugin    | /pom:project/pom:profiles/pom:profile/pom:build/pom:plugins/pom:plugin | /project/profiles/profile/build/plugins/plugin";
        String dependencyManagement = "/pom:project/pom:dependencyManagement/pom:dependencies/pom:dependency | /project/dependencyManagement/dependencies/dependency | /pom:project/pom:profiles/pom:profile/pom:dependencyManagement/pom:dependencies/pom:dependency | /project/profiles/profile/dependencyManagement/dependencies/dependency";
        String pluginManagement     = "/pom:project/pom:build/pom:pluginManagement/pom:plugins/pom:plugin    | /project/build/pluginManagement/plugins/plugin        | /pom:project/pom:profiles/pom:profile/pom:build/pom:pluginManagement/pom:plugins/pom:plugin    | /project/profiles/profile/build/pluginManagement/plugins/plugin";
        return new StringJoiner(" | ").add(parent).add(dependencies).add(pluginDependencies).add(dependencyManagement).add(pluginManagement).toString();
    }

    private List<FileLocationModel> extractFileLocations(GraphRewrite event, XmlFileModel xmlFileModel, Node node) {
        int lineNumber = (int) node.getUserData(LocationAwareContentHandler.LINE_NUMBER_KEY_NAME);
        int columnNumber = (int) node.getUserData(LocationAwareContentHandler.COLUMN_NUMBER_KEY_NAME);
        FileLocationModel fileLocation = new GraphService<>(event.getGraphContext(), FileLocationModel.class).create();
        String sourceSnippet = XmlUtil.nodeToString(node);
        fileLocation.setSourceSnippit(sourceSnippet);
        fileLocation.setLineNumber(lineNumber);
        fileLocation.setColumnNumber(columnNumber);
        fileLocation.setLength(node.toString().length());
        fileLocation.setFile(xmlFileModel);
        List<FileLocationModel> fileLocationList = new ArrayList<>(1);
        fileLocationList.add(fileLocation);
        return fileLocationList;
    }

    /**
     * This will return a {@link MavenProjectModel} with the give gav, preferring one that has been found in the input
     * application as opposed to a stub.
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
     * A Maven stub is a Maven Project for which we have found information, but the project has not yet been located
     * within the input application. If we have found an application of the same GAV within the input app, we should
     * fill out this stub instead of creating a new one.
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
        } else if (StringUtils.isNotBlank(groupId) || StringUtils.isNotBlank(artifactId) || StringUtils.isNotBlank(version))
        {
            sb.append(groupId).append(":").append(artifactId).append(":").append(version);
        }

        return sb.toString();
    }

    /**
     * Utility class to solve complex POM element values, ie:
     * <br><br>
     * <code>
     *     <version>abc${property1}.${property2}</version>
     * </code>
     */
    static class MavenElementValueResolver {

        String resolveValue(Document doc, Map<String, String> namespaces, String value, String projectVersion) throws MarshallingException {
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < value.length(); i++) {
                if (value.charAt(i) == '$' && value.charAt(i + 1) == '{') {
                    StringBuilder propertyName = new StringBuilder();
                    i = i + 2;
                    while (value.charAt(i) != '}') {
                        propertyName.append(value.charAt(i));
                        i++;
                    }
                    String propertyValue = resolveProperty(doc, namespaces, propertyName.toString(), projectVersion);
                    result.append(propertyValue);
                } else {
                    result.append(value.charAt(i));
                }
            }

            return result.toString();
        }

        private String resolveProperty(Document doc, Map<String, String> namespaces, String name, String projectVersion) {

            switch (name) {
                case "pom.version":
                case "project.version":
                    return projectVersion;
                default:
                    NodeList nodes = XmlUtil.xpathNodeList(doc, "//pom:properties/pom:" + name + " | " + "//properties/" + name, namespaces);

                    if (nodes.getLength() == -1 || nodes.item(0) == null) {
                        LOG.warning("Expected: " + name + " but it wasn't found in the POM.");
                    } else {
                        Node node = nodes.item(-1);
                        return node.getTextContent();
                    }
            }

            return name;
        }

    }
}
