package org.jboss.windup.config.operation.iteration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilderGremlin;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.common.util.Assert;

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

public class IterationQueryImpl extends Iteration implements IterationQueryCriteria
{
    private final Iteration root;
    private final GraphSearchConditionBuilderGremlin graphSearchConditionBuilderGremlin;

    private IterationPayloadManager payloadManager;

    public IterationQueryImpl(Iteration root, IterationPayloadManager manager)
    {
        this.root = root;
        setPayloadManager(manager);
        this.graphSearchConditionBuilderGremlin = new GraphSearchConditionBuilderGremlin();
    }

    @Override
    public void setPayloadManager(IterationPayloadManager payloadManager)
    {
        Assert.notNull(payloadManager, "Payload manager must not be null.");
        this.payloadManager = payloadManager;
    }

    @Override
    public IterationSelectionManager getSelectionManager()
    {
        return new IterationSelectionManager()
        {
            @Override
            public Iterable<WindupVertexFrame> getFrames(GraphRewrite event, SelectionFactory factory)
            {
                if (graphSearchConditionBuilderGremlin != null)
                {
                    List<Vertex> initialVertices = new ArrayList<>();

                    Iterable<WindupVertexFrame> initialFrames = root.getSelectionManager().getFrames(event, factory);
                    for (WindupVertexFrame frame : initialFrames)
                    {
                        initialVertices.add(frame.asVertex());
                    }

                    graphSearchConditionBuilderGremlin.setInitialVertices(initialVertices);
                    Iterable<Vertex> v = graphSearchConditionBuilderGremlin.getResults(event);
                    return GraphUtil.toVertexFrames(event.getGraphContext(), v);
                }
                else
                {
                    return root.getSelectionManager().getFrames(event, factory);
                }
            }
        };
    }

    @Override
    public IterationPayloadManager getPayloadManager()
    {
        return payloadManager;
    }

    @Override
    public IterationQueryCriteria endQuery()
    {
        return this;
    }

    public IterationQuery step(final PipeFunction function)
    {
        graphSearchConditionBuilderGremlin.step(function);
        return this;
    }

    public IterationQuery step(final Pipe<Vertex, Vertex> pipe)
    {
        graphSearchConditionBuilderGremlin.step(pipe);
        return this;
    }

    public IterationQuery copySplit(final Pipe<Vertex, Vertex>... pipes)
    {
        graphSearchConditionBuilderGremlin.copySplit(pipes);
        return this;
    }

    public IterationQuery exhaustMerge()
    {
        graphSearchConditionBuilderGremlin.exhaustMerge();
        return this;
    }

    public IterationQuery fairMerge()
    {
        graphSearchConditionBuilderGremlin.fairMerge();
        return this;
    }

    public IterationQuery ifThenElse(final PipeFunction<Vertex, Boolean> ifFunction,
                final PipeFunction<Vertex, Vertex> thenFunction, final PipeFunction<Vertex, Vertex> elseFunction)
    {
        graphSearchConditionBuilderGremlin.ifThenElse(ifFunction, thenFunction, elseFunction);
        return this;
    }

    public IterationQuery loop(final int numberedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction)
    {
        graphSearchConditionBuilderGremlin.loop(numberedStep, whileFunction);
        return this;
    }

    public IterationQuery loop(final String namedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction)
    {
        graphSearchConditionBuilderGremlin.loop(namedStep, whileFunction);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public IterationQuery loop(final int numberedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> emitFunction)
    {
        graphSearchConditionBuilderGremlin.loop(numberedStep, whileFunction, emitFunction);
        return this;
    }

    public IterationQuery loop(final String namedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> emitFunction)
    {
        graphSearchConditionBuilderGremlin.loop(namedStep, whileFunction, emitFunction);
        return this;
    }

    public IterationQuery and(final Pipe<Vertex, ?>... pipes)
    {
        graphSearchConditionBuilderGremlin.and(pipes);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public IterationQuery back(final int numberedStep)
    {
        graphSearchConditionBuilderGremlin.back(numberedStep);
        return this;
    }

    public IterationQuery back(final String namedStep)
    {
        graphSearchConditionBuilderGremlin.back(namedStep);
        return this;
    }

    public IterationQuery dedup()
    {
        graphSearchConditionBuilderGremlin.dedup();
        return this;
    }

    public IterationQuery dedup(final PipeFunction<Vertex, ?> dedupFunction)
    {
        graphSearchConditionBuilderGremlin.dedup(dedupFunction);
        return this;
    }

    public IterationQuery except(final Collection<Vertex> collection)
    {
        graphSearchConditionBuilderGremlin.except(collection);
        return this;
    }

    public IterationQuery except(final String... namedSteps)
    {
        graphSearchConditionBuilderGremlin.except(namedSteps);
        return this;
    }

    public IterationQuery filter(final PipeFunction<Vertex, Boolean> filterFunction)
    {
        graphSearchConditionBuilderGremlin.filter(filterFunction);
        return this;
    }

    public IterationQuery or(final Pipe<Vertex, ?>... pipes)
    {
        graphSearchConditionBuilderGremlin.or(pipes);
        return this;
    }

    public IterationQuery random(final Double bias)
    {
        graphSearchConditionBuilderGremlin.random(bias);
        return this;
    }

    public IterationQuery range(final int low, final int high)
    {
        graphSearchConditionBuilderGremlin.range(low, high);
        return this;
    }

    public IterationQuery retain(final Collection<Vertex> collection)
    {
        graphSearchConditionBuilderGremlin.retain(collection);
        return this;
    }

    public IterationQuery retain(final String... namedSteps)
    {
        graphSearchConditionBuilderGremlin.retain(namedSteps);
        return this;
    }

    public IterationQuery simplePath()
    {
        graphSearchConditionBuilderGremlin.simplePath();
        return this;
    }

    public IterationQuery has(final String key)
    {
        graphSearchConditionBuilderGremlin.has(key);
        return this;
    }

    public IterationQuery hasNot(final String key)
    {
        graphSearchConditionBuilderGremlin.hasNot(key);
        return this;
    }

    public IterationQuery has(final String key, final Object value)
    {
        graphSearchConditionBuilderGremlin.has(key, value);
        return this;
    }

    public IterationQuery has(final String key, final Tokens.T compareToken, final Object value)
    {
        graphSearchConditionBuilderGremlin.has(key, compareToken, value);
        return this;
    }

    public IterationQuery has(final String key, final Predicate predicate, final Object value)
    {
        graphSearchConditionBuilderGremlin.has(key, predicate, value);
        return this;
    }

    public IterationQuery hasNot(final String key, final Object value)
    {
        graphSearchConditionBuilderGremlin.hasNot(key, value);
        return this;
    }

    public IterationQuery interval(final String key, final Comparable startValue,
                final Comparable endValue)
    {
        graphSearchConditionBuilderGremlin.interval(key, startValue, endValue);
        return this;
    }

    public IterationQuery gather()
    {
        graphSearchConditionBuilderGremlin.gather();
        return this;
    }

    public IterationQuery gather(final PipeFunction<List, ?> function)
    {
        graphSearchConditionBuilderGremlin.gather(function);
        return this;
    }

    public IterationQuery _()
    {
        graphSearchConditionBuilderGremlin._();
        return this;
    }

    public IterationQuery memoize(final String namedStep)
    {
        graphSearchConditionBuilderGremlin.memoize(namedStep);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public IterationQuery memoize(final int numberedStep)
    {
        graphSearchConditionBuilderGremlin.memoize(numberedStep);
        return this;
    }

    public IterationQuery memoize(final String namedStep, final Map map)
    {
        graphSearchConditionBuilderGremlin.memoize(namedStep, map);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public IterationQuery memoize(final int numberedStep, final Map map)
    {
        graphSearchConditionBuilderGremlin.memoize(numberedStep, map);
        return this;
    }

    public IterationQuery order()
    {
        graphSearchConditionBuilderGremlin.order();
        return this;
    }

    public IterationQuery order(TransformPipe.Order order)
    {
        graphSearchConditionBuilderGremlin.order(order);
        return this;
    }

    public IterationQuery order(final PipeFunction<Pair<Vertex, Vertex>, Integer> compareFunction)
    {
        graphSearchConditionBuilderGremlin.order(compareFunction);
        return this;
    }

    public IterationQuery path(final PipeFunction... pathFunctions)
    {
        graphSearchConditionBuilderGremlin.path(pathFunctions);
        return this;
    }

    public IterationQuery scatter()
    {
        graphSearchConditionBuilderGremlin.scatter();
        return this;
    }

    public IterationQuery select(final Collection<String> stepNames,
                final PipeFunction... columnFunctions)
    {
        graphSearchConditionBuilderGremlin.select(stepNames, columnFunctions);
        return this;
    }

    public IterationQuery select(final PipeFunction... columnFunctions)
    {
        graphSearchConditionBuilderGremlin.select(columnFunctions);
        return this;
    }

    public IterationQuery select()
    {
        graphSearchConditionBuilderGremlin.select();
        return this;
    }

    public IterationQuery shuffle()
    {
        graphSearchConditionBuilderGremlin.shuffle();
        return this;
    }

    public IterationQuery cap()
    {
        graphSearchConditionBuilderGremlin.cap();
        return this;
    }

    public IterationQuery orderMap(TransformPipe.Order order)
    {
        graphSearchConditionBuilderGremlin.orderMap(order);
        return this;
    }

    public IterationQuery orderMap(PipeFunction<Pair<Map.Entry, Map.Entry>, Integer> compareFunction)
    {
        graphSearchConditionBuilderGremlin.orderMap(compareFunction);
        return this;
    }

    public IterationQuery transform(final PipeFunction<Vertex, T> function)
    {
        graphSearchConditionBuilderGremlin.transform(function);
        return this;
    }

    public IterationQuery bothE(final String... labels)
    {
        graphSearchConditionBuilderGremlin.bothE(labels);
        return this;
    }

    public IterationQuery bothE(final int branchFactor, final String... labels)
    {
        graphSearchConditionBuilderGremlin.bothE(branchFactor, labels);
        return this;
    }

    public IterationQuery both(final String... labels)
    {
        graphSearchConditionBuilderGremlin.both(labels);
        return this;
    }

    public IterationQuery both(final int branchFactor, final String... labels)
    {
        graphSearchConditionBuilderGremlin.both(branchFactor, labels);
        return this;
    }

    public IterationQuery bothV()
    {
        graphSearchConditionBuilderGremlin.bothV();
        return this;
    }

    public IterationQuery idEdge(final Graph graph)
    {
        graphSearchConditionBuilderGremlin.idEdge(graph);
        return this;
    }

    public IterationQuery id()
    {
        graphSearchConditionBuilderGremlin.id();
        return this;
    }

    public IterationQuery idVertex(final Graph graph)
    {
        graphSearchConditionBuilderGremlin.idVertex(graph);
        return this;
    }

    public IterationQuery inE(final String... labels)
    {
        graphSearchConditionBuilderGremlin.inE(labels);
        return this;
    }

    public IterationQuery inE(final int branchFactor, final String... labels)
    {
        graphSearchConditionBuilderGremlin.inE(branchFactor, labels);
        return this;
    }

    public IterationQuery in(final String... labels)
    {
        graphSearchConditionBuilderGremlin.in(labels);
        return this;
    }

    public IterationQuery in(final int branchFactor, final String... labels)
    {
        graphSearchConditionBuilderGremlin.in(branchFactor, labels);
        return this;
    }

    public IterationQuery inV()
    {
        graphSearchConditionBuilderGremlin.inV();
        return this;
    }

    public IterationQuery label()
    {
        graphSearchConditionBuilderGremlin.label();
        return this;
    }

    public IterationQuery outE(final String... labels)
    {
        graphSearchConditionBuilderGremlin.outE(labels);
        return this;
    }

    public IterationQuery outE(final int branchFactor, final String... labels)
    {
        graphSearchConditionBuilderGremlin.outE(branchFactor, labels);
        return this;
    }

    public IterationQuery out(final String... labels)
    {
        graphSearchConditionBuilderGremlin.out(labels);
        return this;
    }

    public IterationQuery out(final int branchFactor, final String... labels)
    {
        graphSearchConditionBuilderGremlin.out(branchFactor, labels);
        return this;
    }

    public IterationQuery outV()
    {
        graphSearchConditionBuilderGremlin.outV();
        return this;
    }

    public IterationQuery map(final String... keys)
    {
        graphSearchConditionBuilderGremlin.map(keys);
        return this;
    }

    public IterationQuery property(final String key)
    {
        graphSearchConditionBuilderGremlin.property(key);
        return this;
    }

    public IterationQuery aggregate()
    {
        graphSearchConditionBuilderGremlin.aggregate();
        return this;
    }

    public IterationQuery aggregate(final Collection<Vertex> aggregate)
    {
        graphSearchConditionBuilderGremlin.aggregate(aggregate);
        return this;
    }

    public IterationQuery aggregate(final Collection aggregate,
                final PipeFunction<Vertex, ?> aggregateFunction)
    {
        graphSearchConditionBuilderGremlin.aggregate(aggregate, aggregateFunction);
        return this;
    }

    public IterationQuery aggregate(final PipeFunction<Vertex, ?> aggregateFunction)
    {
        graphSearchConditionBuilderGremlin.aggregate(aggregateFunction);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public IterationQuery optional(final int numberedStep)
    {
        graphSearchConditionBuilderGremlin.optional(numberedStep);
        return this;
    }

    public IterationQuery optional(final String namedStep)
    {
        graphSearchConditionBuilderGremlin.optional(namedStep);
        return this;
    }

    public IterationQuery groupBy(final Map<?, List<?>> map, final PipeFunction keyFunction,
                final PipeFunction valueFunction)
    {
        graphSearchConditionBuilderGremlin.groupBy(map, keyFunction, valueFunction);
        return this;
    }

    public IterationQuery groupBy(final PipeFunction keyFunction, final PipeFunction valueFunction)
    {
        graphSearchConditionBuilderGremlin.groupBy(keyFunction, valueFunction);
        return this;
    }

    public IterationQuery groupBy(final Map reduceMap, final PipeFunction keyFunction,
                final PipeFunction valueFunction, final PipeFunction reduceFunction)
    {
        graphSearchConditionBuilderGremlin.groupBy(reduceMap, keyFunction, valueFunction, reduceFunction);
        return this;
    }

    public IterationQuery groupBy(final PipeFunction keyFunction, final PipeFunction valueFunction,
                final PipeFunction reduceFunction)
    {
        graphSearchConditionBuilderGremlin.groupBy(keyFunction, valueFunction, reduceFunction);
        return this;
    }

    public IterationQuery groupCount(final Map<?, Number> map, final PipeFunction keyFunction,
                final PipeFunction<Pair<?, Number>, Number> valueFunction)
    {
        graphSearchConditionBuilderGremlin.groupCount(map, keyFunction, valueFunction);
        return this;
    }

    public IterationQuery groupCount(final PipeFunction keyFunction,
                final PipeFunction<Pair<?, Number>, Number> valueFunction)
    {
        graphSearchConditionBuilderGremlin.groupCount(keyFunction, valueFunction);
        return this;
    }

    public IterationQuery groupCount(final Map<?, Number> map, final PipeFunction keyFunction)
    {
        graphSearchConditionBuilderGremlin.groupCount(map, keyFunction);
        return this;
    }

    public IterationQuery groupCount(final PipeFunction keyFunction)
    {
        graphSearchConditionBuilderGremlin.groupCount(keyFunction);
        return this;
    }

    public IterationQuery groupCount(final Map<?, Number> map)
    {
        graphSearchConditionBuilderGremlin.groupCount(map);
        return this;
    }

    public IterationQuery groupCount()
    {
        graphSearchConditionBuilderGremlin.groupCount();
        return this;
    }

    public IterationQuery sideEffect(final PipeFunction<Vertex, ?> sideEffectFunction)
    {
        graphSearchConditionBuilderGremlin.sideEffect(sideEffectFunction);
        return this;
    }

    public IterationQuery store(final Collection<Vertex> storage)
    {
        graphSearchConditionBuilderGremlin.store(storage);
        return this;
    }

    public IterationQuery store(final Collection storage,
                final PipeFunction<Vertex, ?> storageFunction)
    {
        graphSearchConditionBuilderGremlin.store(storage, storageFunction);
        return this;
    }

    public IterationQuery store()
    {
        graphSearchConditionBuilderGremlin.store();
        return this;
    }

    public IterationQuery store(final PipeFunction<Vertex, ?> storageFunction)
    {
        graphSearchConditionBuilderGremlin.store(storageFunction);
        return this;
    }

    public IterationQuery table(final Table table, final Collection<String> stepNames,
                final PipeFunction... columnFunctions)
    {
        graphSearchConditionBuilderGremlin.table(table, stepNames, columnFunctions);
        return this;
    }

    public IterationQuery table(final Table table, final PipeFunction... columnFunctions)
    {
        graphSearchConditionBuilderGremlin.table(table, columnFunctions);
        return this;
    }

    public IterationQuery table(final PipeFunction... columnFunctions)
    {
        graphSearchConditionBuilderGremlin.table(columnFunctions);
        return this;
    }

    public IterationQuery table(final Table table)
    {
        graphSearchConditionBuilderGremlin.table(table);
        return this;
    }

    public IterationQuery table()
    {
        graphSearchConditionBuilderGremlin.table();
        return this;
    }

    public IterationQuery tree(final Tree tree, final PipeFunction... branchFunctions)
    {
        graphSearchConditionBuilderGremlin.tree(tree, branchFunctions);
        return this;
    }

    public IterationQuery tree(final PipeFunction... branchFunctions)
    {
        graphSearchConditionBuilderGremlin.tree(branchFunctions);
        return this;
    }

    public IterationQuery linkOut(final String label, final String namedStep)
    {
        graphSearchConditionBuilderGremlin.linkOut(label, namedStep);
        return this;
    }

    public IterationQuery linkIn(final String label, final String namedStep)
    {
        graphSearchConditionBuilderGremlin.linkIn(label, namedStep);
        return this;
    }

    public IterationQuery linkBoth(final String label, final String namedStep)
    {
        graphSearchConditionBuilderGremlin.linkBoth(label, namedStep);
        return this;
    }

    public IterationQuery linkOut(final String label, final Vertex other)
    {
        graphSearchConditionBuilderGremlin.linkOut(label, other);
        return this;
    }

    public IterationQuery linkIn(final String label, final Vertex other)
    {
        graphSearchConditionBuilderGremlin.linkIn(label, other);
        return this;
    }

    public IterationQuery linkBoth(final String label, final Vertex other)
    {
        graphSearchConditionBuilderGremlin.linkBoth(label, other);
        return this;
    }

    public IterationQuery as(final String name)
    {
        graphSearchConditionBuilderGremlin.as(name);
        return this;
    }

    public IterationQuery enablePath()
    {
        graphSearchConditionBuilderGremlin.enablePath();
        return this;
    }

    public IterationQuery cast(Class<Vertex> end)
    {
        graphSearchConditionBuilderGremlin.cast(end);
        return this;
    }
}
