package org.jboss.windup.reporting.service;


import com.tinkerpop.blueprints.Vertex;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *  @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
abstract class MapSumEffortAccumulatorFunction<T> implements EffortAccumulatorFunction
{
    private final Map<T, Integer> results;


    public MapSumEffortAccumulatorFunction()
    {
        this(new HashMap<T, Integer>());
    }

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

    public Map<T, Integer> getResults()
    {
        return results;
    }

    public abstract T vertexToKey(Vertex vertex);
}
