package org.jboss.windup.rules.apps.mavenize;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.LinkedMap;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A simplified POM model - just G:A:V:C:P and the dependencies.
 *
 * @author Ondrej Zizka, zizka at seznam.cz
 */
public class Pom implements Dependency {
    MavenCoord coord = new MavenCoord();
    Pom parent;
    MavenCoord bom;
    String name;
    String description;
    Set<Dependency> dependencies = new LinkedHashSet<>();
    Set<Pom> localDependencies = new LinkedHashSet<>();
    OrderedMap<String, Pom> submodules = new LinkedMap<>();
    boolean root = false;
    ModuleRole role = ModuleRole.NORMAL;

    Pom(MavenCoord coord) {
        this.coord = coord;
    }

    /**
     * Contains the parent.
     */
    public Pom getParent() {
        return parent;
    }

    /**
     * Contains the parent.
     */
    public Pom setParent(Pom parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Contains the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Contains the name.
     */
    public Pom setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Contains the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Contains the description.
     */
    public Pom setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Third-party library dependencies.
     */
    public Set<Dependency> getDependencies() {
        return dependencies;
    }

    /**
     * Third-party library dependencies.
     */
    public Pom setDependencies(Set<Dependency> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    /**
     * Dependencies on other modules of the project.
     */
    public Set<Pom> getLocalDependencies() {
        return localDependencies;
    }

    /**
     * Dependencies on other modules of the project.
     */
    public Pom setLocalDependencies(Set<Pom> localDependencies) {
        this.localDependencies = localDependencies;
        return this;
    }

    /**
     * Contains submodules of this Maven module.
     */
    public OrderedMap<String, Pom> getSubmodules() {
        return submodules;
    }

    /**
     * Contains submodules of this Maven module.
     */
    public Pom setSubmodules(OrderedMap<String, Pom> submodules) {
        this.submodules = submodules;
        return this;
    }

    /**
     * The root pom of the project.
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * The root pom of the project.
     */
    public Pom setRoot(boolean root) {
        this.root = root;
        return this;
    }

    /**
     * Returns a {@link MavenCoord} representing the Maven coordinates of this POM.
     */
    public MavenCoord getCoord() {
        return coord;
    }

    /**
     * Sets a {@link MavenCoord} representing the Maven coordinates of this POM.
     */

    public Pom setCoord(MavenCoord coord) {
        this.coord = coord;
        return this;
    }

    /**
     * Gets the Bom used by this POM.
     */
    public MavenCoord getBom() {
        return bom;
    }

    /**
     * Sets the Bom used by this POM.
     */
    public Pom setBom(MavenCoord bom) {
        this.bom = bom;
        return this;
    }

    @Override
    public String toString() {
        return "Pom{" + role + " " + coord + ", parent=" + (parent == null ? "" : parent.coord) + ", " + "name=" + name
                + /* ", desc=" + description + */ ", " + "dependencies=" + CollectionUtils.size(dependencies) + ", " + "localDependencies="
                + CollectionUtils.size(localDependencies) + ", " + "submodules=" + CollectionUtils.size(submodules) + '}';
    }

    @Override
    public Role getRole() {
        return Role.MODULE;
    }

    enum ModuleRole {
        PARENT, BOM, NORMAL
    }
}
