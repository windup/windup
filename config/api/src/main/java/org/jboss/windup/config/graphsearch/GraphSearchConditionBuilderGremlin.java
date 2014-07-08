package org.jboss.windup.config.graphsearch;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.runner.VarStack;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.Tokens;
import com.tinkerpop.gremlin.Tokens.T;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.LoopPipe;
import com.tinkerpop.pipes.transform.TransformPipe;
import com.tinkerpop.pipes.util.structures.Pair;
import com.tinkerpop.pipes.util.structures.Table;
import com.tinkerpop.pipes.util.structures.Tree;

/**
 * Provides access to the full GremlinPipeline API:
 * <ul>
 * <li><a href="https://github.com/tinkerpop/gremlin/wiki">Gremlin Wiki</a></li>
 * <li><a href="https://github.com/tinkerpop/gremlin/wiki/Gremlin-Steps">Gremlin Steps (Cheatsheet)</a></li>
 * <li><a
 * href="http://www.tinkerpop.com/docs/javadocs/gremlin/2.4.0/com/tinkerpop/gremlin/java/GremlinPipeline.html">Gremlin
 * Pipeline Javadoc</a></li>
 * </ul>
 * 
 * @author jsightler
 * 
 */
public class GraphSearchConditionBuilderGremlin extends GraphCondition
{
    private String variableName;
    private GraphSearchConditionBuilder graphSearchConditionBuilder;
    private Iterable<Vertex> initialVertices;

    private GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>();

    public GraphSearchConditionBuilderGremlin()
    {

    }

    public GraphSearchConditionBuilderGremlin(GraphSearchConditionBuilder graphSearchConditionBuilder)
    {
        this.variableName = graphSearchConditionBuilder.getVariableName();
        this.graphSearchConditionBuilder = graphSearchConditionBuilder;
    }

    public GraphSearchConditionBuilderGremlin(String collectionName, Iterable<Vertex> initialVertices)
    {
        this.variableName = collectionName;
        this.initialVertices = initialVertices;
    }

    public static GraphSearchConditionBuilderGremlin create(String collectionName, Iterable<Vertex> initial)
    {
        return new GraphSearchConditionBuilderGremlin(collectionName, initial);
    }

    public void setInitialVertices(Iterable<Vertex> initialVertices)
    {
        this.initialVertices = initialVertices;
    }

    public Iterable<Vertex> getResults(GraphRewrite event)
    {
        Iterable<Vertex> vertices;
        if (initialVertices == null)
        {
            vertices = graphSearchConditionBuilder.getResults(event);
        }
        else
        {
            vertices = initialVertices;
        }
        pipeline.setStarts(vertices);

        return pipeline;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        Set<WindupVertexFrame> frames = new HashSet<>();
        for (Vertex v : getResults(event))
        {
            WindupVertexFrame frame = event.getGraphContext().getFramed().frame(v, WindupVertexFrame.class);
            frames.add(frame);
        }

        VarStack varStack = (VarStack) event.getRewriteContext().get(VarStack.class);
        varStack.setVariable(variableName, frames);

        return !frames.isEmpty();
    }

    // -- Gremlin pipeline methods --

    public GraphSearchConditionBuilderGremlin V()
    {
        pipeline.V();
        return this;
    }

    public GraphSearchConditionBuilderGremlin V(String key, Object value)
    {
        pipeline.V(key, value);
        return this;
    }

    public GraphSearchConditionBuilderGremlin framedType(Class<? extends WindupVertexFrame> clazz)
    {
        GraphSearchCriterionType.addPipeFor(pipeline, clazz);
        return this;
    }

    // <editor-fold defaultstate="collapsed" desc="Gremlin Pipes wrapper methods">

    public GraphSearchConditionBuilderGremlin step(final PipeFunction function)
    {
        pipeline.step(function);
        return this;
    }

    public GraphSearchConditionBuilderGremlin step(final Pipe<Vertex, Vertex> pipe)
    {
        pipeline.step(pipe);
        return this;
    }

    public GraphSearchConditionBuilderGremlin copySplit(final Pipe<Vertex, Vertex>... pipes)
    {
        pipeline.copySplit(pipes);
        return this;
    }

    public GraphSearchConditionBuilderGremlin exhaustMerge()
    {
        pipeline.exhaustMerge();
        return this;
    }

    public GraphSearchConditionBuilderGremlin fairMerge()
    {
        pipeline.fairMerge();
        return this;
    }

    public GraphSearchConditionBuilderGremlin ifThenElse(final PipeFunction<Vertex, Boolean> ifFunction,
                final PipeFunction<Vertex, Vertex> thenFunction, final PipeFunction<Vertex, Vertex> elseFunction)
    {
        pipeline.ifThenElse(ifFunction, thenFunction, elseFunction);
        return this;
    }

    @SuppressWarnings("deprecation")
    public GraphSearchConditionBuilderGremlin loop(final int numberedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction)
    {
        pipeline.loop(numberedStep, whileFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin loop(final String namedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction)
    {
        pipeline.loop(namedStep, whileFunction);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public GraphSearchConditionBuilderGremlin loop(final int numberedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> emitFunction)
    {
        pipeline.loop(numberedStep, whileFunction, emitFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin loop(final String namedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> emitFunction)
    {
        pipeline.loop(namedStep, whileFunction, emitFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin and(final Pipe<Vertex, ?>... pipes)
    {
        pipeline.and(pipes);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public GraphSearchConditionBuilderGremlin back(final int numberedStep)
    {
        pipeline.back(numberedStep);
        return this;
    }

    public GraphSearchConditionBuilderGremlin back(final String namedStep)
    {
        pipeline.back(namedStep);
        return this;
    }

    public GraphSearchConditionBuilderGremlin dedup()
    {
        pipeline.dedup();
        return this;
    }

    public GraphSearchConditionBuilderGremlin dedup(final PipeFunction<Vertex, ?> dedupFunction)
    {
        pipeline.dedup(dedupFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin except(final Collection<Vertex> collection)
    {
        pipeline.except(collection);
        return this;
    }

    public GraphSearchConditionBuilderGremlin except(final String... namedSteps)
    {
        pipeline.except(namedSteps);
        return this;
    }

    public GraphSearchConditionBuilderGremlin filter(final PipeFunction<Vertex, Boolean> filterFunction)
    {
        pipeline.filter(filterFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin or(final Pipe<Vertex, ?>... pipes)
    {
        pipeline.or(pipes);
        return this;
    }

    public GraphSearchConditionBuilderGremlin random(final Double bias)
    {
        pipeline.random(bias);
        return this;
    }

    public GraphSearchConditionBuilderGremlin range(final int low, final int high)
    {
        pipeline.range(low, high);
        return this;
    }

    public GraphSearchConditionBuilderGremlin retain(final Collection<Vertex> collection)
    {
        pipeline.retain(collection);
        return this;
    }

    public GraphSearchConditionBuilderGremlin retain(final String... namedSteps)
    {
        pipeline.retain(namedSteps);
        return this;
    }

    public GraphSearchConditionBuilderGremlin simplePath()
    {
        pipeline.simplePath();
        return this;
    }

    public GraphSearchConditionBuilderGremlin has(final String key)
    {
        pipeline.has(key);
        return this;
    }

    public GraphSearchConditionBuilderGremlin hasNot(final String key)
    {
        pipeline.hasNot(key);
        return this;
    }

    public GraphSearchConditionBuilderGremlin has(final String key, final Object value)
    {
        pipeline.has(key, value);
        return this;
    }

    public GraphSearchConditionBuilderGremlin has(final String key, final Tokens.T compareToken, final Object value)
    {
        pipeline.has(key, compareToken, value);
        return this;
    }

    public GraphSearchConditionBuilderGremlin has(final String key, final Predicate predicate, final Object value)
    {
        pipeline.has(key, predicate, value);
        return this;
    }

    public GraphSearchConditionBuilderGremlin hasNot(final String key, final Object value)
    {
        pipeline.hasNot(key, value);
        return this;
    }

    public GraphSearchConditionBuilderGremlin interval(final String key, final Comparable startValue,
                final Comparable endValue)
    {
        pipeline.interval(key, startValue, endValue);
        return this;
    }

    public GraphSearchConditionBuilderGremlin gather()
    {
        pipeline.gather();
        return this;
    }

    public GraphSearchConditionBuilderGremlin gather(final PipeFunction<List, ?> function)
    {
        pipeline.gather(function);
        return this;
    }

    public GraphSearchConditionBuilderGremlin _()
    {
        pipeline._();
        return this;
    }

    public GraphSearchConditionBuilderGremlin memoize(final String namedStep)
    {
        pipeline.memoize(namedStep);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public GraphSearchConditionBuilderGremlin memoize(final int numberedStep)
    {
        pipeline.memoize(numberedStep);
        return this;
    }

    public GraphSearchConditionBuilderGremlin memoize(final String namedStep, final Map map)
    {
        pipeline.memoize(namedStep, map);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public GraphSearchConditionBuilderGremlin memoize(final int numberedStep, final Map map)
    {
        pipeline.memoize(numberedStep, map);
        return this;
    }

    public GraphSearchConditionBuilderGremlin order()
    {
        pipeline.order();
        return this;
    }

    public GraphSearchConditionBuilderGremlin order(TransformPipe.Order order)
    {
        pipeline.order(order);
        return this;
    }

    public GraphSearchConditionBuilderGremlin order(final PipeFunction<Pair<Vertex, Vertex>, Integer> compareFunction)
    {
        pipeline.order(compareFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin path(final PipeFunction... pathFunctions)
    {
        pipeline.path(pathFunctions);
        return this;
    }

    public GraphSearchConditionBuilderGremlin scatter()
    {
        pipeline.scatter();
        return this;
    }

    public GraphSearchConditionBuilderGremlin select(final Collection<String> stepNames,
                final PipeFunction... columnFunctions)
    {
        pipeline.select(stepNames, columnFunctions);
        return this;
    }

    public GraphSearchConditionBuilderGremlin select(final PipeFunction... columnFunctions)
    {
        pipeline.select(columnFunctions);
        return this;
    }

    public GraphSearchConditionBuilderGremlin select()
    {
        pipeline.select();
        return this;
    }

    public GraphSearchConditionBuilderGremlin shuffle()
    {
        pipeline.shuffle();
        return this;
    }

    public GraphSearchConditionBuilderGremlin cap()
    {
        pipeline.cap();
        return this;
    }

    public GraphSearchConditionBuilderGremlin orderMap(TransformPipe.Order order)
    {
        pipeline.orderMap(order);
        return this;
    }

    public GraphSearchConditionBuilderGremlin orderMap(PipeFunction<Pair<Map.Entry, Map.Entry>, Integer> compareFunction)
    {
        pipeline.orderMap(compareFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin transform(final PipeFunction<Vertex, T> function)
    {
        pipeline.transform(function);
        return this;
    }

    public GraphSearchConditionBuilderGremlin bothE(final String... labels)
    {
        pipeline.bothE(labels);
        return this;
    }

    public GraphSearchConditionBuilderGremlin bothE(final int branchFactor, final String... labels)
    {
        pipeline.bothE(branchFactor, labels);
        return this;
    }

    public GraphSearchConditionBuilderGremlin both(final String... labels)
    {
        pipeline.both(labels);
        return this;
    }

    public GraphSearchConditionBuilderGremlin both(final int branchFactor, final String... labels)
    {
        pipeline.both(branchFactor, labels);
        return this;
    }

    public GraphSearchConditionBuilderGremlin bothV()
    {
        pipeline.bothV();
        return this;
    }

    public GraphSearchConditionBuilderGremlin idEdge(final Graph graph)
    {
        pipeline.idEdge(graph);
        return this;
    }

    public GraphSearchConditionBuilderGremlin id()
    {
        pipeline.id();
        return this;
    }

    public GraphSearchConditionBuilderGremlin idVertex(final Graph graph)
    {
        pipeline.idVertex(graph);
        return this;
    }

    public GraphSearchConditionBuilderGremlin inE(final String... labels)
    {
        pipeline.inE(labels);
        return this;
    }

    public GraphSearchConditionBuilderGremlin inE(final int branchFactor, final String... labels)
    {
        pipeline.inE(branchFactor, labels);
        return this;
    }

    public GraphSearchConditionBuilderGremlin in(final String... labels)
    {
        pipeline.in(labels);
        return this;
    }

    public GraphSearchConditionBuilderGremlin in(final int branchFactor, final String... labels)
    {
        pipeline.in(branchFactor, labels);
        return this;
    }

    public GraphSearchConditionBuilderGremlin inV()
    {
        pipeline.inV();
        return this;
    }

    public GraphSearchConditionBuilderGremlin label()
    {
        pipeline.label();
        return this;
    }

    public GraphSearchConditionBuilderGremlin outE(final String... labels)
    {
        pipeline.outE(labels);
        return this;
    }

    public GraphSearchConditionBuilderGremlin outE(final int branchFactor, final String... labels)
    {
        pipeline.outE(branchFactor, labels);
        return this;
    }

    public GraphSearchConditionBuilderGremlin out(final String... labels)
    {
        pipeline.out(labels);
        return this;
    }

    public GraphSearchConditionBuilderGremlin out(final int branchFactor, final String... labels)
    {
        pipeline.out(branchFactor, labels);
        return this;
    }

    public GraphSearchConditionBuilderGremlin outV()
    {
        pipeline.outV();
        return this;
    }

    public GraphSearchConditionBuilderGremlin map(final String... keys)
    {
        pipeline.map(keys);
        return this;
    }

    public GraphSearchConditionBuilderGremlin property(final String key)
    {
        pipeline.property(key);
        return this;
    }

    public GraphSearchConditionBuilderGremlin aggregate()
    {
        pipeline.aggregate();
        return this;
    }

    public GraphSearchConditionBuilderGremlin aggregate(final Collection<Vertex> aggregate)
    {
        pipeline.aggregate(aggregate);
        return this;
    }

    public GraphSearchConditionBuilderGremlin aggregate(final Collection aggregate,
                final PipeFunction<Vertex, ?> aggregateFunction)
    {
        pipeline.aggregate(aggregate, aggregateFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin aggregate(final PipeFunction<Vertex, ?> aggregateFunction)
    {
        pipeline.aggregate(aggregateFunction);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public GraphSearchConditionBuilderGremlin optional(final int numberedStep)
    {
        pipeline.optional(numberedStep);
        return this;
    }

    public GraphSearchConditionBuilderGremlin optional(final String namedStep)
    {
        pipeline.optional(namedStep);
        return this;
    }

    public GraphSearchConditionBuilderGremlin groupBy(final Map<?, List<?>> map, final PipeFunction keyFunction,
                final PipeFunction valueFunction)
    {
        pipeline.groupBy(map, keyFunction, valueFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin groupBy(final PipeFunction keyFunction, final PipeFunction valueFunction)
    {
        pipeline.groupBy(keyFunction, valueFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin groupBy(final Map reduceMap, final PipeFunction keyFunction,
                final PipeFunction valueFunction, final PipeFunction reduceFunction)
    {
        pipeline.groupBy(reduceMap, keyFunction, valueFunction, reduceFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin groupBy(final PipeFunction keyFunction, final PipeFunction valueFunction,
                final PipeFunction reduceFunction)
    {
        pipeline.groupBy(keyFunction, valueFunction, reduceFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin groupCount(final Map<?, Number> map, final PipeFunction keyFunction,
                final PipeFunction<Pair<?, Number>, Number> valueFunction)
    {
        pipeline.groupCount(map, keyFunction, valueFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin groupCount(final PipeFunction keyFunction,
                final PipeFunction<Pair<?, Number>, Number> valueFunction)
    {
        pipeline.groupCount(keyFunction, valueFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin groupCount(final Map<?, Number> map, final PipeFunction keyFunction)
    {
        pipeline.groupCount(map, keyFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin groupCount(final PipeFunction keyFunction)
    {
        pipeline.groupCount(keyFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin groupCount(final Map<?, Number> map)
    {
        pipeline.groupCount(map);
        return this;
    }

    public GraphSearchConditionBuilderGremlin groupCount()
    {
        pipeline.groupCount();
        return this;
    }

    public GraphSearchConditionBuilderGremlin sideEffect(final PipeFunction<Vertex, ?> sideEffectFunction)
    {
        pipeline.sideEffect(sideEffectFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin store(final Collection<Vertex> storage)
    {
        pipeline.store(storage);
        return this;
    }

    public GraphSearchConditionBuilderGremlin store(final Collection storage,
                final PipeFunction<Vertex, ?> storageFunction)
    {
        pipeline.store(storage, storageFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin store()
    {
        pipeline.store();
        return this;
    }

    public GraphSearchConditionBuilderGremlin store(final PipeFunction<Vertex, ?> storageFunction)
    {
        pipeline.store(storageFunction);
        return this;
    }

    public GraphSearchConditionBuilderGremlin table(final Table table, final Collection<String> stepNames,
                final PipeFunction... columnFunctions)
    {
        pipeline.table(table, stepNames, columnFunctions);
        return this;
    }

    public GraphSearchConditionBuilderGremlin table(final Table table, final PipeFunction... columnFunctions)
    {
        pipeline.table(table, columnFunctions);
        return this;
    }

    public GraphSearchConditionBuilderGremlin table(final PipeFunction... columnFunctions)
    {
        pipeline.table(columnFunctions);
        return this;
    }

    public GraphSearchConditionBuilderGremlin table(final Table table)
    {
        pipeline.table(table);
        return this;
    }

    public GraphSearchConditionBuilderGremlin table()
    {
        pipeline.table();
        return this;
    }

    public GraphSearchConditionBuilderGremlin tree(final Tree tree, final PipeFunction... branchFunctions)
    {
        pipeline.tree(tree, branchFunctions);
        return this;
    }

    public GraphSearchConditionBuilderGremlin tree(final PipeFunction... branchFunctions)
    {
        pipeline.tree(branchFunctions);
        return this;
    }

    public GraphSearchConditionBuilderGremlin linkOut(final String label, final String namedStep)
    {
        pipeline.linkOut(label, namedStep);
        return this;
    }

    public GraphSearchConditionBuilderGremlin linkIn(final String label, final String namedStep)
    {
        pipeline.linkIn(label, namedStep);
        return this;
    }

    public GraphSearchConditionBuilderGremlin linkBoth(final String label, final String namedStep)
    {
        pipeline.linkBoth(label, namedStep);
        return this;
    }

    public GraphSearchConditionBuilderGremlin linkOut(final String label, final Vertex other)
    {
        pipeline.linkOut(label, other);
        return this;
    }

    public GraphSearchConditionBuilderGremlin linkIn(final String label, final Vertex other)
    {
        pipeline.linkIn(label, other);
        return this;
    }

    public GraphSearchConditionBuilderGremlin linkBoth(final String label, final Vertex other)
    {
        pipeline.linkBoth(label, other);
        return this;
    }

    public GraphSearchConditionBuilderGremlin named(final String name)
    {
        pipeline.as(name);
        return this;
    }

    public GraphSearchConditionBuilderGremlin enablePath()
    {
        pipeline.enablePath();
        return this;
    }

    public GraphSearchConditionBuilderGremlin cast(Class<Vertex> end)
    {
        pipeline.cast(end);
        return this;
    }
    // </editor-fold>
}
