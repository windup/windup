package org.jboss.windup.config.operation.iteration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilderGremlin;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.util.GraphUtil;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.Tokens;
import com.tinkerpop.gremlin.Tokens.T;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.LoopPipe;
import com.tinkerpop.pipes.transform.TransformPipe;
import com.tinkerpop.pipes.util.structures.Pair;
import com.tinkerpop.pipes.util.structures.Table;
import com.tinkerpop.pipes.util.structures.Tree;

/**
 * Gremlin Pipes adapter for Iteration.queryFor( ... ).
 */
public class GremlinPipesQueryImpl extends Iteration implements IterationQueryCriteria
{
    private final Iteration root;
    private final GraphSearchConditionBuilderGremlin graphSearchConditionBuilderGremlin;

    public GremlinPipesQueryImpl(Iteration root, IterationPayloadManager manager)
    {
        this.root = root;
        this.root.setPayloadManager(manager);
        this.graphSearchConditionBuilderGremlin = new GraphSearchConditionBuilderGremlin();
    }

    /**
     * @returns A SelectionManager which performs a Gremlin query.
     */
    @Override
    public IterationSelectionManager getSelectionManager()
    {
        return new GremlinIterationSelectionManager();
    }

    /**
     * 
     */
    private class GremlinIterationSelectionManager implements IterationSelectionManager
    {
        @Override
        public Iterable<WindupVertexFrame> getFrames(GraphRewrite event, Variables varStack)
        {
            List<Vertex> initialVertices = getInitialVertices(event, varStack);

            // Perform the query and convert to frames.
            graphSearchConditionBuilderGremlin.setInitialVertices(initialVertices);
            Iterable<Vertex> v = graphSearchConditionBuilderGremlin.getResults(event);
            return GraphUtil.toVertexFrames(event.getGraphContext(), v);
        }

        /**
         * The initial vertices are those matched by previous query constructs. Iteration.[initial
         * vertices].queryFor().[gremlin pipe wrappers]
         */
        private List<Vertex> getInitialVertices(GraphRewrite event, Variables varStack)
        {
            List<Vertex> initialVertices = new ArrayList<>();
            Iterable<WindupVertexFrame> initialFrames = root.getSelectionManager().getFrames(event, varStack);
            // TODO: Doesn't the root SelectionManager have the same event and varStack?
            for (WindupVertexFrame frame : initialFrames)
                initialVertices.add(frame.asVertex());
            return initialVertices;
        }
    }

    @Override
    public void setPayloadManager(IterationPayloadManager payloadManager)
    {
        root.setPayloadManager(payloadManager);
    }

    @Override
    public IterationPayloadManager getPayloadManager()
    {
        return root.getPayloadManager();
    }

    @Override
    public IterationQueryCriteria endQuery()
    {
        return this;
    }

    // <editor-fold defaultstate="collapsed" desc="Gremlin pipes wrapping methods.">

    public GremlinPipesQuery step(final PipeFunction function)
    {
        graphSearchConditionBuilderGremlin.step(function);
        return this;
    }

    public GremlinPipesQuery step(final Pipe<Vertex, Vertex> pipe)
    {
        graphSearchConditionBuilderGremlin.step(pipe);
        return this;
    }

    public GremlinPipesQuery copySplit(final Pipe<Vertex, Vertex>... pipes)
    {
        graphSearchConditionBuilderGremlin.copySplit(pipes);
        return this;
    }

    public GremlinPipesQuery exhaustMerge()
    {
        graphSearchConditionBuilderGremlin.exhaustMerge();
        return this;
    }

    public GremlinPipesQuery fairMerge()
    {
        graphSearchConditionBuilderGremlin.fairMerge();
        return this;
    }

    public GremlinPipesQuery ifThenElse(final PipeFunction<Vertex, Boolean> ifFunction,
                final PipeFunction<Vertex, Vertex> thenFunction, final PipeFunction<Vertex, Vertex> elseFunction)
    {
        graphSearchConditionBuilderGremlin.ifThenElse(ifFunction, thenFunction, elseFunction);
        return this;
    }

    public GremlinPipesQuery loop(final int numberedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction)
    {
        graphSearchConditionBuilderGremlin.loop(numberedStep, whileFunction);
        return this;
    }

    public GremlinPipesQuery loop(final String namedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction)
    {
        graphSearchConditionBuilderGremlin.loop(namedStep, whileFunction);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public GremlinPipesQuery loop(final int numberedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> emitFunction)
    {
        graphSearchConditionBuilderGremlin.loop(numberedStep, whileFunction, emitFunction);
        return this;
    }

    public GremlinPipesQuery loop(final String namedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> emitFunction)
    {
        graphSearchConditionBuilderGremlin.loop(namedStep, whileFunction, emitFunction);
        return this;
    }

    public GremlinPipesQuery and(final Pipe<Vertex, ?>... pipes)
    {
        graphSearchConditionBuilderGremlin.and(pipes);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public GremlinPipesQuery back(final int numberedStep)
    {
        graphSearchConditionBuilderGremlin.back(numberedStep);
        return this;
    }

    public GremlinPipesQuery back(final String namedStep)
    {
        graphSearchConditionBuilderGremlin.back(namedStep);
        return this;
    }

    public GremlinPipesQuery dedup()
    {
        graphSearchConditionBuilderGremlin.dedup();
        return this;
    }

    public GremlinPipesQuery dedup(final PipeFunction<Vertex, ?> dedupFunction)
    {
        graphSearchConditionBuilderGremlin.dedup(dedupFunction);
        return this;
    }

    public GremlinPipesQuery except(final Collection<Vertex> collection)
    {
        graphSearchConditionBuilderGremlin.except(collection);
        return this;
    }

    public GremlinPipesQuery except(final String... namedSteps)
    {
        graphSearchConditionBuilderGremlin.except(namedSteps);
        return this;
    }

    public GremlinPipesQuery filter(final PipeFunction<Vertex, Boolean> filterFunction)
    {
        graphSearchConditionBuilderGremlin.filter(filterFunction);
        return this;
    }

    public GremlinPipesQuery or(final Pipe<Vertex, ?>... pipes)
    {
        graphSearchConditionBuilderGremlin.or(pipes);
        return this;
    }

    public GremlinPipesQuery random(final Double bias)
    {
        graphSearchConditionBuilderGremlin.random(bias);
        return this;
    }

    public GremlinPipesQuery range(final int low, final int high)
    {
        graphSearchConditionBuilderGremlin.range(low, high);
        return this;
    }

    public GremlinPipesQuery retain(final Collection<Vertex> collection)
    {
        graphSearchConditionBuilderGremlin.retain(collection);
        return this;
    }

    public GremlinPipesQuery retain(final String... namedSteps)
    {
        graphSearchConditionBuilderGremlin.retain(namedSteps);
        return this;
    }

    public GremlinPipesQuery simplePath()
    {
        graphSearchConditionBuilderGremlin.simplePath();
        return this;
    }

    public GremlinPipesQuery has(final String key)
    {
        graphSearchConditionBuilderGremlin.has(key);
        return this;
    }

    public GremlinPipesQuery hasNot(final String key)
    {
        graphSearchConditionBuilderGremlin.hasNot(key);
        return this;
    }

    public GremlinPipesQuery has(final String key, final Object value)
    {
        graphSearchConditionBuilderGremlin.has(key, value);
        return this;
    }

    public GremlinPipesQuery has(final String key, final Tokens.T compareToken, final Object value)
    {
        graphSearchConditionBuilderGremlin.has(key, compareToken, value);
        return this;
    }

    public GremlinPipesQuery has(final String key, final Predicate predicate, final Object value)
    {
        graphSearchConditionBuilderGremlin.has(key, predicate, value);
        return this;
    }

    public GremlinPipesQuery hasNot(final String key, final Object value)
    {
        graphSearchConditionBuilderGremlin.hasNot(key, value);
        return this;
    }

    public GremlinPipesQuery interval(final String key, final Comparable startValue,
                final Comparable endValue)
    {
        graphSearchConditionBuilderGremlin.interval(key, startValue, endValue);
        return this;
    }

    public GremlinPipesQuery gather()
    {
        graphSearchConditionBuilderGremlin.gather();
        return this;
    }

    public GremlinPipesQuery gather(final PipeFunction<List, ?> function)
    {
        graphSearchConditionBuilderGremlin.gather(function);
        return this;
    }

    public GremlinPipesQuery _()
    {
        graphSearchConditionBuilderGremlin._();
        return this;
    }

    public GremlinPipesQuery memoize(final String namedStep)
    {
        graphSearchConditionBuilderGremlin.memoize(namedStep);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public GremlinPipesQuery memoize(final int numberedStep)
    {
        graphSearchConditionBuilderGremlin.memoize(numberedStep);
        return this;
    }

    public GremlinPipesQuery memoize(final String namedStep, final Map map)
    {
        graphSearchConditionBuilderGremlin.memoize(namedStep, map);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public GremlinPipesQuery memoize(final int numberedStep, final Map map)
    {
        graphSearchConditionBuilderGremlin.memoize(numberedStep, map);
        return this;
    }

    public GremlinPipesQuery order()
    {
        graphSearchConditionBuilderGremlin.order();
        return this;
    }

    public GremlinPipesQuery order(TransformPipe.Order order)
    {
        graphSearchConditionBuilderGremlin.order(order);
        return this;
    }

    public GremlinPipesQuery order(final PipeFunction<Pair<Vertex, Vertex>, Integer> compareFunction)
    {
        graphSearchConditionBuilderGremlin.order(compareFunction);
        return this;
    }

    public GremlinPipesQuery path(final PipeFunction... pathFunctions)
    {
        graphSearchConditionBuilderGremlin.path(pathFunctions);
        return this;
    }

    public GremlinPipesQuery scatter()
    {
        graphSearchConditionBuilderGremlin.scatter();
        return this;
    }

    public GremlinPipesQuery select(final Collection<String> stepNames,
                final PipeFunction... columnFunctions)
    {
        graphSearchConditionBuilderGremlin.select(stepNames, columnFunctions);
        return this;
    }

    public GremlinPipesQuery select(final PipeFunction... columnFunctions)
    {
        graphSearchConditionBuilderGremlin.select(columnFunctions);
        return this;
    }

    public GremlinPipesQuery select()
    {
        graphSearchConditionBuilderGremlin.select();
        return this;
    }

    public GremlinPipesQuery shuffle()
    {
        graphSearchConditionBuilderGremlin.shuffle();
        return this;
    }

    public GremlinPipesQuery cap()
    {
        graphSearchConditionBuilderGremlin.cap();
        return this;
    }

    public GremlinPipesQuery orderMap(TransformPipe.Order order)
    {
        graphSearchConditionBuilderGremlin.orderMap(order);
        return this;
    }

    public GremlinPipesQuery orderMap(PipeFunction<Pair<Map.Entry, Map.Entry>, Integer> compareFunction)
    {
        graphSearchConditionBuilderGremlin.orderMap(compareFunction);
        return this;
    }

    public GremlinPipesQuery transform(final PipeFunction<Vertex, T> function)
    {
        graphSearchConditionBuilderGremlin.transform(function);
        return this;
    }

    public GremlinPipesQuery bothE(final String... labels)
    {
        graphSearchConditionBuilderGremlin.bothE(labels);
        return this;
    }

    public GremlinPipesQuery bothE(final int branchFactor, final String... labels)
    {
        graphSearchConditionBuilderGremlin.bothE(branchFactor, labels);
        return this;
    }

    public GremlinPipesQuery both(final String... labels)
    {
        graphSearchConditionBuilderGremlin.both(labels);
        return this;
    }

    public GremlinPipesQuery both(final int branchFactor, final String... labels)
    {
        graphSearchConditionBuilderGremlin.both(branchFactor, labels);
        return this;
    }

    public GremlinPipesQuery bothV()
    {
        graphSearchConditionBuilderGremlin.bothV();
        return this;
    }

    public GremlinPipesQuery idEdge(final Graph graph)
    {
        graphSearchConditionBuilderGremlin.idEdge(graph);
        return this;
    }

    public GremlinPipesQuery id()
    {
        graphSearchConditionBuilderGremlin.id();
        return this;
    }

    public GremlinPipesQuery idVertex(final Graph graph)
    {
        graphSearchConditionBuilderGremlin.idVertex(graph);
        return this;
    }

    public GremlinPipesQuery inE(final String... labels)
    {
        graphSearchConditionBuilderGremlin.inE(labels);
        return this;
    }

    public GremlinPipesQuery inE(final int branchFactor, final String... labels)
    {
        graphSearchConditionBuilderGremlin.inE(branchFactor, labels);
        return this;
    }

    public GremlinPipesQuery in(final String... labels)
    {
        graphSearchConditionBuilderGremlin.in(labels);
        return this;
    }

    public GremlinPipesQuery in(final int branchFactor, final String... labels)
    {
        graphSearchConditionBuilderGremlin.in(branchFactor, labels);
        return this;
    }

    public GremlinPipesQuery inV()
    {
        graphSearchConditionBuilderGremlin.inV();
        return this;
    }

    public GremlinPipesQuery label()
    {
        graphSearchConditionBuilderGremlin.label();
        return this;
    }

    public GremlinPipesQuery outE(final String... labels)
    {
        graphSearchConditionBuilderGremlin.outE(labels);
        return this;
    }

    public GremlinPipesQuery outE(final int branchFactor, final String... labels)
    {
        graphSearchConditionBuilderGremlin.outE(branchFactor, labels);
        return this;
    }

    public GremlinPipesQuery out(final String... labels)
    {
        graphSearchConditionBuilderGremlin.out(labels);
        return this;
    }

    public GremlinPipesQuery out(final int branchFactor, final String... labels)
    {
        graphSearchConditionBuilderGremlin.out(branchFactor, labels);
        return this;
    }

    public GremlinPipesQuery outV()
    {
        graphSearchConditionBuilderGremlin.outV();
        return this;
    }

    public GremlinPipesQuery map(final String... keys)
    {
        graphSearchConditionBuilderGremlin.map(keys);
        return this;
    }

    public GremlinPipesQuery property(final String key)
    {
        graphSearchConditionBuilderGremlin.property(key);
        return this;
    }

    public GremlinPipesQuery aggregate()
    {
        graphSearchConditionBuilderGremlin.aggregate();
        return this;
    }

    public GremlinPipesQuery aggregate(final Collection<Vertex> aggregate)
    {
        graphSearchConditionBuilderGremlin.aggregate(aggregate);
        return this;
    }

    public GremlinPipesQuery aggregate(final Collection aggregate,
                final PipeFunction<Vertex, ?> aggregateFunction)
    {
        graphSearchConditionBuilderGremlin.aggregate(aggregate, aggregateFunction);
        return this;
    }

    public GremlinPipesQuery aggregate(final PipeFunction<Vertex, ?> aggregateFunction)
    {
        graphSearchConditionBuilderGremlin.aggregate(aggregateFunction);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public GremlinPipesQuery optional(final int numberedStep)
    {
        graphSearchConditionBuilderGremlin.optional(numberedStep);
        return this;
    }

    public GremlinPipesQuery optional(final String namedStep)
    {
        graphSearchConditionBuilderGremlin.optional(namedStep);
        return this;
    }

    public GremlinPipesQuery groupBy(final Map<?, List<?>> map, final PipeFunction keyFunction,
                final PipeFunction valueFunction)
    {
        graphSearchConditionBuilderGremlin.groupBy(map, keyFunction, valueFunction);
        return this;
    }

    public GremlinPipesQuery groupBy(final PipeFunction keyFunction, final PipeFunction valueFunction)
    {
        graphSearchConditionBuilderGremlin.groupBy(keyFunction, valueFunction);
        return this;
    }

    public GremlinPipesQuery groupBy(final Map reduceMap, final PipeFunction keyFunction,
                final PipeFunction valueFunction, final PipeFunction reduceFunction)
    {
        graphSearchConditionBuilderGremlin.groupBy(reduceMap, keyFunction, valueFunction, reduceFunction);
        return this;
    }

    public GremlinPipesQuery groupBy(final PipeFunction keyFunction, final PipeFunction valueFunction,
                final PipeFunction reduceFunction)
    {
        graphSearchConditionBuilderGremlin.groupBy(keyFunction, valueFunction, reduceFunction);
        return this;
    }

    public GremlinPipesQuery groupCount(final Map<?, Number> map, final PipeFunction keyFunction,
                final PipeFunction<Pair<?, Number>, Number> valueFunction)
    {
        graphSearchConditionBuilderGremlin.groupCount(map, keyFunction, valueFunction);
        return this;
    }

    public GremlinPipesQuery groupCount(final PipeFunction keyFunction,
                final PipeFunction<Pair<?, Number>, Number> valueFunction)
    {
        graphSearchConditionBuilderGremlin.groupCount(keyFunction, valueFunction);
        return this;
    }

    public GremlinPipesQuery groupCount(final Map<?, Number> map, final PipeFunction keyFunction)
    {
        graphSearchConditionBuilderGremlin.groupCount(map, keyFunction);
        return this;
    }

    public GremlinPipesQuery groupCount(final PipeFunction keyFunction)
    {
        graphSearchConditionBuilderGremlin.groupCount(keyFunction);
        return this;
    }

    public GremlinPipesQuery groupCount(final Map<?, Number> map)
    {
        graphSearchConditionBuilderGremlin.groupCount(map);
        return this;
    }

    public GremlinPipesQuery groupCount()
    {
        graphSearchConditionBuilderGremlin.groupCount();
        return this;
    }

    public GremlinPipesQuery sideEffect(final PipeFunction<Vertex, ?> sideEffectFunction)
    {
        graphSearchConditionBuilderGremlin.sideEffect(sideEffectFunction);
        return this;
    }

    public GremlinPipesQuery store(final Collection<Vertex> storage)
    {
        graphSearchConditionBuilderGremlin.store(storage);
        return this;
    }

    public GremlinPipesQuery store(final Collection storage,
                final PipeFunction<Vertex, ?> storageFunction)
    {
        graphSearchConditionBuilderGremlin.store(storage, storageFunction);
        return this;
    }

    public GremlinPipesQuery store()
    {
        graphSearchConditionBuilderGremlin.store();
        return this;
    }

    public GremlinPipesQuery store(final PipeFunction<Vertex, ?> storageFunction)
    {
        graphSearchConditionBuilderGremlin.store(storageFunction);
        return this;
    }

    public GremlinPipesQuery table(final Table table, final Collection<String> stepNames,
                final PipeFunction... columnFunctions)
    {
        graphSearchConditionBuilderGremlin.table(table, stepNames, columnFunctions);
        return this;
    }

    public GremlinPipesQuery table(final Table table, final PipeFunction... columnFunctions)
    {
        graphSearchConditionBuilderGremlin.table(table, columnFunctions);
        return this;
    }

    public GremlinPipesQuery table(final PipeFunction... columnFunctions)
    {
        graphSearchConditionBuilderGremlin.table(columnFunctions);
        return this;
    }

    public GremlinPipesQuery table(final Table table)
    {
        graphSearchConditionBuilderGremlin.table(table);
        return this;
    }

    public GremlinPipesQuery table()
    {
        graphSearchConditionBuilderGremlin.table();
        return this;
    }

    public GremlinPipesQuery tree(final Tree tree, final PipeFunction... branchFunctions)
    {
        graphSearchConditionBuilderGremlin.tree(tree, branchFunctions);
        return this;
    }

    public GremlinPipesQuery tree(final PipeFunction... branchFunctions)
    {
        graphSearchConditionBuilderGremlin.tree(branchFunctions);
        return this;
    }

    public GremlinPipesQuery linkOut(final String label, final String namedStep)
    {
        graphSearchConditionBuilderGremlin.linkOut(label, namedStep);
        return this;
    }

    public GremlinPipesQuery linkIn(final String label, final String namedStep)
    {
        graphSearchConditionBuilderGremlin.linkIn(label, namedStep);
        return this;
    }

    public GremlinPipesQuery linkBoth(final String label, final String namedStep)
    {
        graphSearchConditionBuilderGremlin.linkBoth(label, namedStep);
        return this;
    }

    public GremlinPipesQuery linkOut(final String label, final Vertex other)
    {
        graphSearchConditionBuilderGremlin.linkOut(label, other);
        return this;
    }

    public GremlinPipesQuery linkIn(final String label, final Vertex other)
    {
        graphSearchConditionBuilderGremlin.linkIn(label, other);
        return this;
    }

    public GremlinPipesQuery linkBoth(final String label, final Vertex other)
    {
        graphSearchConditionBuilderGremlin.linkBoth(label, other);
        return this;
    }

    public GremlinPipesQuery named(final String name)
    {
        graphSearchConditionBuilderGremlin.named(name);
        return this;
    }

    public GremlinPipesQuery enablePath()
    {
        graphSearchConditionBuilderGremlin.enablePath();
        return this;
    }

    public GremlinPipesQuery cast(Class<Vertex> end)
    {
        graphSearchConditionBuilderGremlin.cast(end);
        return this;
    }
    // </editor-fold>

}
