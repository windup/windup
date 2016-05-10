package org.jboss.windup.rules.apps.mavenize;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.OrganizationModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TechnologyReferenceModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.util.Logging;

/**
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, ozizka at seznam.cz</a>
 */
public class MavenizationService
{
    private static final Logger LOG = Logging.get(MavenizationService.class);
    public static final String OUTPUT_SUBDIR_MAVENIZED = "mavenized";

    private final GraphContext grCtx;


    MavenizationService(GraphContext graphContext)
    {
        this.grCtx = graphContext;
    }

    /**
     * For the given application (Windup input), creates a stub of Mavenized project.
     * <p>
     * The resulting structure is: (+--- is "module", +~~~ is a dependency)
     * <pre>
     *  Parent POM
     *   +--- BOM
     *     +~~~ JBoss EAP BOM
     *   +--- JAR submodule
     *     +~~~ library JARs
     *   +--- WAR
     *     +~~~ library JARs
     *     +~~~ JAR submodule
     *   +--- EAR
     *     +~~~ library JAR
     *     +~~~ JAR submodule
     *     +~~~ WAR submodule
     * </pre>
     */
    void mavenizeApp(ProjectModel projectModel)
    {
        LOG.info("Mavenizing  ProjectModel " + projectModel.toPrettyString());
        MavenizationContext mavCtx = new MavenizationContext();
        mavCtx.graphContext = grCtx;

        WindupConfigurationModel config = grCtx.getUnique(WindupConfigurationModel.class);
        ///mavCtx.mavenizedBaseDir = Paths.get("/tmp/mavenized");
        mavCtx.mavenizedBaseDir = config.getOutputPath().asFile().toPath().resolve(OUTPUT_SUBDIR_MAVENIZED);
        mavCtx.unifiedGroupId = new ModuleAnalysisHelper(grCtx).deriveGroupId(projectModel);
        mavCtx.unifiedAppName = normalizeDirName(projectModel.getName());
        mavCtx.unifiedVersion = "1.0";

        // 1) create the overall structure - a parent, and a BOM.

        // Root pom.xml ( == parent pom.xml in our resulting structure).
        mavCtx.rootPom = new Pom(new MavenCoord(mavCtx.getUnifiedGroupId(), mavCtx.getUnifiedAppName() + "-parent", mavCtx.getUnifiedVersion()));
        mavCtx.rootPom.role = Pom.ModuleRole.PARENT;
        ///PomXmlModel rootPom = grCtx.service(PomXmlModel.class).create();
        mavCtx.rootPom.parent = new Pom(MavenizeRuleProvider.JBOSS_PARENT);
        mavCtx.rootPom.name = projectModel.getName() + " - Parent";
        mavCtx.rootPom.description = "Parent of " + projectModel.getName();
        mavCtx.rootPom.root = true;
        final String bomArtifactId = mavCtx.getUnifiedAppName() + "-bom";

        // BOM
        Pom bom = new Pom(new MavenCoord(mavCtx.getUnifiedGroupId(), bomArtifactId, mavCtx.getUnifiedVersion()));
        bom.bom = getTargetTechnologies().contains("eap7")
                ? MavenizeRuleProvider.JBOSS_BOM_JAVAEE7_WITH_ALL
                : MavenizeRuleProvider.JBOSS_BOM_JAVAEE6_WITH_ALL;
        bom.role = Pom.ModuleRole.BOM;
        bom.parent = new Pom(MavenizeRuleProvider.JBOSS_PARENT);
        bom.description = "Bill of Materials. See https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html";
        bom.name = projectModel.getName() + " - BOM";
        mavCtx.getRootPom().submodules.put(bomArtifactId, bom);
        mavCtx.bom = bom;

        // BOM - dependencyManagement dependencies
        for( ArchiveCoordinateModel dep : grCtx.getUnique(GlobalBomModel.class).getDependencies() ){
            LOG.info("Adding dep to BOM: " + dep.toPrettyString());
            bom.dependencies.add(new SimpleDependency(Dependency.Role.LIBRARY, new MavenCoord(dep)));
        }

        // 2) Recursively add the modules.
        mavCtx.rootAppPom = mavenizeModule(mavCtx, projectModel, null);

        // Sort the modules.
        ///mavCtx.rootPom.submodules = sortSubmodulesToReflectDependencies(mavCtx.rootAppPom);

        // 3) Write the pom.xml's.
        new MavenStructureRenderer(mavCtx).createMavenProjectDirectoryTree();
    }


    /**
     * Mavenizes the particular project module, i.e. single pom.xml.
     * Then continues recursively to submodules.
     * @return null if the project can't be processed for some reason, e.g. is from an unparsable jar.
     */
    private Pom mavenizeModule(MavenizationContext mavCtx, ProjectModel projectModel, Pom containingModule)
    {
        LOG.info("Mavenizing submodule ProjectModel " + projectModel.toPrettyString());
        LOG.info("    Root file: " + projectModel.getRootFileModel().toPrettyString());
        LOG.info("    Containing module: " + containingModule);

        if (projectModel.getRootFileModel().getParseError() != null)
            return null;

        // Known library -> add as a dependency and skip.

        // SHA1 identified archive?
        if (projectModel.getRootFileModel() instanceof IdentifiedArchiveModel)
        {
            final IdentifiedArchiveModel idArch = (IdentifiedArchiveModel)projectModel.getRootFileModel();
            if (idArch == null)
                LOG.warning("Project's IdentifiedArchiveModel getRootFileModel() returned null.");
            else if (idArch.getCoordinate() == null)
                LOG.warning("Project's IdentifiedArchiveModel getRootFileModel().getCoordinate() returned null.");
            else if(containingModule == null)
                LOG.warning("containingModule is null."); // IllegalStateEx?
            else
                containingModule.dependencies.add(new SimpleDependency(Dependency.Role.LIBRARY, MavenCoord.from(idArch.getCoordinate())));

            LOG.info("Known library, skipping recursive mavenization: " + idArch.getCoordinate());
            return null;
        }

        // ArchiveModel's organizations contain a known org, like Apache.
        if (projectModel.getRootFileModel() instanceof ArchiveModel)
        {
            Set<String> skipOrganizations = new HashSet(Arrays.asList("Apache Sun Iona IBM Codehaus Spring Sonatype JBoss Oracle".toLowerCase().split("")));

            final ArchiveModel arch = (ArchiveModel)projectModel.getRootFileModel();
            for (OrganizationModel org : arch.getOrganizationModels())
            {
                if (skipOrganizations.contains(org.getName().toLowerCase()))
                {
                    LOG.info("Library from 3rd party vendor ("+org.getName()+"), skipping recursive mavenization: " + arch.getFilePath());
                    return null;
                }
            }
        }

        // A MavenProject.
        // TODO: This covers both app modules and libraries with pom.xml inside. Need to diferentiate - skip only libraries.
        // TODO: This should disappear after WINDUP-981
        if (false && projectModel instanceof MavenProjectModel)
        {
            final MavenProjectModel mvnProject = (MavenProjectModel)projectModel;
            if (mvnProject.getMavenIdentifier() == null)
                LOG.warning("MavenProject's getMavenIdentifier() returned null.");
            else if(containingModule == null)
                LOG.warning("containingModule is null."); // IllegalStateEx?
            else if (true /* isLibraryAndNotProjectModule() */)
                containingModule.dependencies.add(new SimpleDependency(Dependency.Role.LIBRARY, MavenCoord.fromGAVPC(mvnProject.getMavenIdentifier())));
            return null;
        }

        MavenCoord modulePomCoords = new MavenCoord();
        modulePomCoords.setGroupId(mavCtx.getUnifiedGroupId());
        modulePomCoords.setVersion(mavCtx.getUnifiedVersion());
        final String artifactId = deriveAppropriateArtifactId(projectModel);
        modulePomCoords.setArtifactId(artifactId);
        modulePomCoords.setPackaging(guessPackaging(projectModel));

        Pom modulePom = new Pom(modulePomCoords);
        modulePom.role = Pom.ModuleRole.NORMAL;
        modulePom.parent = mavCtx.getRootPom();
        mavCtx.getRootPom().submodules.put(artifactId, modulePom);
        mavCtx.getKnownSubmodules().add(modulePom);


        // Set up the dependency of the containing module on the contained module. E.g. EAR depends on WAR.
        if(containingModule != null)
            ///containingModule.dependencies.add(new SimpleDependency(Dependency.Role.MODULE, modulePom.coord));
            containingModule.dependencies.add(modulePom);

        // Nested archives
        // For now, only count with the modules of this app. There are likely more in the other apps.
        Set<ArchiveModel> nestedModules = new HashSet<>();
        for (FileModel file : projectModel.getFileModelsNoDirectories())
        {
            if(!(file instanceof ArchiveModel)) //  TODO: Query for ArchiveModel directly.
                continue;

            // Known library -> simple dependency.
            if(file instanceof IdentifiedArchiveModel){
                IdentifiedArchiveModel artifact = (IdentifiedArchiveModel) file;
                modulePom.dependencies.add(new SimpleDependency(Dependency.Role.LIBRARY, new MavenCoord(artifact.getCoordinate())));
            }
            // Unknown archives -> nested modules? -> local dependencies.
            else {
                nestedModules.add((ArchiveModel) file);
            }
        }

        // Nested modules already identified as ProjectModel
        for (ProjectModel subProject : projectModel.getChildProjects())
        {
            Pom subModulePom = mavenizeModule(mavCtx, subProject, modulePom);
            if (subModulePom == null)
                continue;
            ///modulePom.dependencies.add(new SimpleDependency(Dependency.Role.MODULE, subModulePom.coord));
            modulePom.dependencies.add(subModulePom);
        }

        // Nested module candidates.
        for (ArchiveModel nestedModule : nestedModules)
        {
            // TODO: Is it a submodule or a library? Does it appear in multiple applications?
            //Pom subModulePom = mavenizeModule(mavCtx, nestedModule, containingModule);
            //modulePom.dependencies.add(subModulePom.identification);
        }

        // TODO: Determine and add the project internal dependencies (on other submodules, cross-app)
        // Queues? JNDI? CDI? REST + WS endpoints?
        // Needs to be done after initial mavenization of all apps to have their G:A:V.

        // TODO: Determine and add compile-time (API) dependencies (like, Java EE API's)
        // One big Java EE API vs. individual?

        // TODO: Remove the deps versions overriding the BOM.
        new FeatureBasedApiDependenciesDeducer(mavCtx).addAppropriateDependencies(projectModel, modulePom);

        return modulePom;
    }


    /**
     * Normalizes the name so it can be used as Maven artifactId or groupId.
     */
    private static String normalizeDirName(String name)
    {
        if(name == null)
            return null;
        return name.toLowerCase().replaceAll("[^a-zA-Z0-9]", "-"); //\p{Alnum}
    }

    /**
     * Tries to guess the packaging of the archive - whether it's an EAR, WAR, JAR.
     * Maybe not needed as we can rely on the suffix?
     */
    private static String guessPackaging(ProjectModel projectModel)
    {
        String projectType = projectModel.getProjectType();
        if (projectType != null)
            return projectType;

        LOG.warning("WINDUP-983 getProjectType() returned null for: " + projectModel.getRootFileModel().getPrettyPath());

        String suffix = StringUtils.substringAfterLast(projectModel.getRootFileModel().getFileName(), ".");
        if ("jar war ear sar har ".contains(suffix+" ")){
            projectModel.setProjectType(suffix); // FIXME: Remove when WINDUP-983 is fixed.
            return suffix;
        }

        // TODO: Should we try something more? Used APIs? What if it's a source?

        return "unknown";
    }


    String deriveAppropriateArtifactId(ProjectModel projectModel)
    {
        String resultName = null;
        String name = projectModel.getName();
        name: {
            if (name == null)
                break name;
            if (name.length() > 40)
                break name;
            if (name.contains(" "))
                break name;
            resultName = name;
        }

        if (resultName == null)
        {
            resultName = projectModel.getRootFileModel().getFileName();
        }

        resultName = removeVersion(resultName);
        resultName = normalizeDirName(resultName);

        if (resultName == null)
            resultName = "unknownName-" + RandomStringUtils.randomAlphanumeric(4);

        // See WINDUP-1015
        if (resultName.length() > 40)
            resultName = resultName.substring(0,40) + "-" + RandomStringUtils.randomAlphanumeric(4);


        return resultName;
    }


    /**
     * Remove 1.0.0 from foo-1.0.0.jar
     */
    String removeVersion(String resultName)
    {
        if(resultName == null)
            return null;

        // Regex test at http://fiddle.re/dnwhca
        return resultName.replaceFirst("[-_]\\d+(\\.\\d+)+(-SNAPSHOT|[.-_](?i:CR|RC|GA|Alpha|Beta|b|milestone|m|Final|RELEASE)[.-_]?\\d+|-incubating)?(?=[-_.]\\w+)", "");
    }


    /**
     * Sorts the submodules of given Pom so that their cross-dependencies are satisfied if built in that order.
     * TODO.
     */
    private OrderedMap<String, Pom> sortSubmodulesToReflectDependencies(Pom pom)
    {
        Set<MavenCoord> dependenciesMet = new HashSet();
        dependenciesMet.add(pom.coord);

        SortedSet<MavenCoord> dependenciesSatisfied = new TreeSet<>();

        for (Dependency dep : pom.dependencies)
        {
            // TODO: Traverse the tree, depth-first, take items at node exit.
        }

        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Context of the mavenization - things to carry around.
     */
    static class MavenizationContext
    {
        private Path mavenizedBaseDir;
        private Pom rootPom;
        private Set<Pom> knownSubmodules = new HashSet<>();
        private String unifiedVersion;
        private String unifiedGroupId;
        private String unifiedAppName;
        private Pom bom; // BOM shared by all other submodules.
        private GraphContext graphContext;
        private Pom rootAppPom;


        public Path getMavenizedBaseDir() {
            return mavenizedBaseDir;
        }

        public Pom getRootPom() {
            return rootPom;
        }

        public Set<Pom> getKnownSubmodules() {
            return knownSubmodules;
        }

        public String getUnifiedVersion() {
            return unifiedVersion;
        }

        public String getUnifiedGroupId() {
            return unifiedGroupId;
        }

        public String getUnifiedAppName() {
            return unifiedAppName;
        }

        public Pom getBom() {
            return bom;
        }

        public GraphContext getGraphContext() {
            return graphContext;
        }

        public Pom getRootAppPom()
        {
            return rootAppPom;
        }

        public void setRootAppPom(Pom rootAppPom)
        {
            this.rootAppPom = rootAppPom;
        }
    }

    /**
     * Used to determine which BOM to take.
     * Currently we only have these tags: eap, eap7.
     */
    private Set<String> getTargetTechnologies()
    {
        WindupConfigurationModel wc = grCtx.getUnique(WindupConfigurationModel.class);
        Iterable<TechnologyReferenceModel> targetTechnologies = wc.getTargetTechnologies();
        Set<String> techs = new HashSet<>();
        for (TechnologyReferenceModel tech : targetTechnologies)
        {
            techs.add(tech.getTechnologyID());
        }
        return techs;
    }

}
