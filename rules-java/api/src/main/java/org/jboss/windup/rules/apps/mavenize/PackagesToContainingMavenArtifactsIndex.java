package org.jboss.windup.rules.apps.mavenize;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import java.util.Collections;
import java.util.logging.Logger;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestFactory;
import org.jboss.windup.util.Logging;

/**
 * This is a service class that provides information about which artifacts contain the given package.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class PackagesToContainingMavenArtifactsIndex
{
    private static final Logger LOG = Logging.get(PackagesToContainingMavenArtifactsIndex.class);
    public static final String EDGE_USES = "uses";

    private final GraphContext graphContext;


    public PackagesToContainingMavenArtifactsIndex(GraphContext graphContext)
    {
        this.graphContext = graphContext;
    }


    /**
     * Which projcets contain classes which reference the given package (in their imports).
     */
    private Iterable<ProjectModel> getProjectsContainingClassesReferencingPackage(String pkg)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    /**
     * For given API artifact, finds the projects whose Java classes use artifact's classes,
     * and links them in the graph.
     *
     * TODO:
     * Because the graph doesn't contain data about all Java classes,
     * this will likely need to use TypeInterestFactory -
     * register all the classes (or just packages?) from the API jars.
     * This is executed in a separate rule.
     */
    public void registerPackagesFromAPI(MavenCoord apiCoords)
    {
        Iterable<String> packages = this.getPackagesInArtifact(apiCoords);
        for (String pkg : packages)
        {
            this.registerPackageInTypeInterestFactory(pkg);
        }
    }

    /**
     * After the packages are registered and Java scanning done,
     * we can link the ProjectModel and API packages together.
     * ProjectModel --uses--> ArchiveCoordinateModel
     */
    public void markProjectsUsingPackagesFromAPI(MavenCoord apiCoords)
    {
        final GraphService<ArchiveCoordinateModel> coordsService = graphContext.service(ArchiveCoordinateModel.class);

        Iterable<String> packages = this.getPackagesInArtifact(apiCoords);
        for (String pkg : packages)
        {
            Iterable<ProjectModel> projects = this.getProjectsContainingClassesReferencingPackage(pkg);
            for (ProjectModel project : projects)
            {
                ArchiveCoordinateModel apiArchiveRepresentant = new ArchiveCoordinateService(graphContext, ArchiveCoordinateModel.class)
                        .getSingleOrCreate(apiCoords.getGroupId(), apiCoords.getArtifactId(), null); // We specifically want null.
                project.asVertex().addEdge(EDGE_USES, apiArchiveRepresentant.asVertex());
            }
        }
    }



    /**
     * For given API artifact, finds the projects whose Java classes use artifact's classes,
     * and links them in the graph.
     */
    public boolean moduleContainsPackagesFromAPI(ProjectModel projectModel, MavenCoord apiCoords)
    {
        ArchiveCoordinateModel archive = new ArchiveCoordinateService(graphContext, ArchiveCoordinateModel.class).findSingle(apiCoords.getGroupId(), apiCoords.getArtifactId(), null);
        if (archive == null)
            return false;
        //return graphContext.testIncidence(projectModel.asVertex(), archive.asVertex(), EDGE_USES);
        Iterable<Vertex> projectsVerts = archive.asVertex().getVertices(Direction.IN, EDGE_USES);
        Iterable<ProjectModel> projects = graphContext.getFramed().frameVertices(projectsVerts, ProjectModel.class);
        for (ProjectModel project : projects)
        {
            if (projectModel.equals(project))
                    return true;
        }
        return false;
    }

    private Iterable<String> getPackagesInArtifact(MavenCoord apiCoords)
    {
        // TODO: Either take from index, or download and scan (Jandex?).
        return Collections.EMPTY_LIST;
    }


    /**
     * So that we get these packages caught Java class analysis.
     */
    private void registerPackageInTypeInterestFactory(String pkg)
    {
        // TODO
        TypeInterestFactory.registerInterest(pkg + "_pkg", pkg.replace(".", "\\."), pkg, TypeReferenceLocation.IMPORT);
    }

}
