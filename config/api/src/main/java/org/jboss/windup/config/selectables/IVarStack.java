package org.jboss.windup.config.selectables;

import java.util.Map;

import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface IVarStack
{ // temp name, Jess and Lincoln don't like this cool way of marking an iface

    /**
     * Finds the specified variables (whether in the current selection or in the variable stack) and returns them as a
     * map. This is especially useful when passing into other APIs (for example the FreeMarker API) to provide easy
     * access to multiple variables at once).
     * 
     * @param varNames
     * @return
     */
    public Map<String, Object> findAllVariablesAsMap(String... varNames);

    /**
     * Type-safe wrapper around findVariable which gives only one framed vertex, and checks if there is 0 or 1; throws
     * otherwise.
     */
    @SuppressWarnings(value = "unchecked")
    <T extends WindupVertexFrame> T findSingletonVariable(Class<T> type, String name);

    /**
     * Searches the variables layers, top to bottom, for given name, and returns if found; null otherwise.
     */
    Iterable<WindupVertexFrame> findVariable(String name);

    /**
     * Remove the top variables layer from the the stack.
     */
    Map<String, Iterable<WindupVertexFrame>> pop();

    /**
     * Add new variables layer on top of the stack.
     */
    void push();

    /**
     * Set a variable in the top variables layer to given "collection" of the vertex frames. Can't be reassigned -
     * throws on attempt to reassign.
     */
    void setVariable(String name, Iterable<WindupVertexFrame> iterable);

}
