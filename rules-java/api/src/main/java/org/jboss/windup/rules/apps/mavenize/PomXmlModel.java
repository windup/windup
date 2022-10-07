package org.jboss.windup.rules.apps.mavenize;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;

import java.util.List;


/**
 * Represents the structure of a Maven POM file within the graph.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 * @see MavenProjectModel
 */
@TypeValue(PomXmlModel.TYPE)
public interface PomXmlModel extends WindupVertexFrame {
    String TYPE = "PomXmlModel";
    String DEPENDS = TYPE + "-dependensOn";
    String PARENT = TYPE + "-parent";
    String BOM = TYPE + "-bom";

    /**
     * Contains the coordinates of the parent POM.
     */
    @Adjacency(label = PARENT, direction = Direction.OUT)
    ArchiveCoordinateModel getParent();

    /**
     * Contains the coordinates of the parent POM.
     */
    @Adjacency(label = PARENT, direction = Direction.OUT)
    void setParent(ArchiveCoordinateModel parent);

    /**
     * Contains the coordinates of the BOM.
     */
    @Adjacency(label = BOM, direction = Direction.OUT)
    ArchiveCoordinateModel getBom();

    /**
     * Contains the coordinates of the BOM.
     */
    @Adjacency(label = BOM, direction = Direction.OUT)
    void setBom(ArchiveCoordinateModel bom);

    /**
     * Contains the coordinates of all dependencies.
     */
    @Adjacency(label = DEPENDS, direction = Direction.OUT)
    List<ArchiveCoordinateModel> getDependencies();

    /**
     * Contains the coordinates of all dependencies.
     */
    @Adjacency(label = DEPENDS, direction = Direction.OUT)
    void setDependencies(Iterable<ArchiveCoordinateModel> deps);

    /**
     * Contains the coordinates of all dependencies.
     */
    @Adjacency(label = DEPENDS, direction = Direction.OUT)
    void addDependency(ArchiveCoordinateModel dependency);
}
