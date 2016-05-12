package org.jboss.windup.rules.apps.mavenize;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;


/**
 * Represents the structure of a Maven POM file within the graph.
 *
 * @see MavenProjectModel
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
@TypeValue(PomXmlModel.TYPE)
public interface PomXmlModel extends WindupVertexFrame
{
    String TYPE = "PomXml";
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
    Iterable<ArchiveCoordinateModel> getDependencies();

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
