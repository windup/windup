package org.jboss.windup.reporting.service;


import org.apache.tinkerpop.gremlin.structure.Vertex;
import java.util.HashMap;
import java.util.Map;

/**
 * This implementation holds a Map, and for each vertex, it increases the key
 * that is determined from the vertex by supplied implementation of vertexToKey({@link Vertex}).
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
abstract class MapSumEffortAccumulatorFunction<T> implements EffortAccumulatorFunction
{
    private final Map<T, Integer> results;

    /**
     * Create a new instance of {@link MapSumEffortAccumulatorFunction} with no starting values.
     */
    public MapSumEffortAccumulatorFunction()
    {
        this(new HashMap<T, Integer>());
    }

    /**
     * Creates a new instance of the accumulator with the given baseline values.
     */
    public MapSumEffortAccumulatorFunction(Map<T, Integer> results)
    {
        this.results = results;
    }

    @Override
    public void accumulate(Vertex effortReportVertex)
    {
        T key = vertexToKey(effortReportVertex);
        if (!results.containsKey(key))
            results.put(key, 1);
        else
            results.put(key, results.get(key) + 1);
    }

    /**
     * Gets the current results.
     */
    public Map<T, Integer> getResults()
    {
        return results;
    }

    /**
     * This extracts the accumulator key from the provided {@link Vertex}. For example, this might be "Severity"
     * or the number of effort points, depending upon what data is being accumulated.
     */
    public abstract T vertexToKey(Vertex vertex);
}
