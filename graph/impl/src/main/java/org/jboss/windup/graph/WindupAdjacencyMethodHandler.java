package org.jboss.windup.graph;

import com.syncleus.ferma.ClassInitializer;
import com.syncleus.ferma.VertexFrame;
import com.syncleus.ferma.framefactories.annotation.AbstractMethodHandler;
import com.syncleus.ferma.framefactories.annotation.CachesReflection;
import com.syncleus.ferma.framefactories.annotation.ReflectionUtility;
import com.syncleus.ferma.typeresolvers.TypeResolver;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;


/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class WindupAdjacencyMethodHandler extends AbstractMethodHandler {

    @Override
    public Class<Adjacency> getAnnotationType() {
        return Adjacency.class;
    }

    @Override
    public <E> DynamicType.Builder<E> processMethod(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        final java.lang.reflect.Parameter[] arguments = method.getParameters();

        if (ReflectionUtility.isAddMethod(method))
            if (arguments == null || arguments.length == 0)
                return this.addVertexDefault(builder, method, annotation);
            else if (arguments.length == 1)
                if (ClassInitializer.class.isAssignableFrom(arguments[0].getType()))
                    return this.addVertexByTypeUntypedEdge(builder, method, annotation);
                else
                    return this.addVertexByObjectUntypedEdge(builder, method, annotation);
            else if (arguments.length == 2) {
                if (!(ClassInitializer.class.isAssignableFrom(arguments[1].getType())))
                    throw new IllegalStateException(method.getName() + " was annotated with @Adjacency, had two arguments, but the second argument was not of the type ClassInitializer");

                if (ClassInitializer.class.isAssignableFrom(arguments[0].getType()))
                    return this.addVertexByTypeTypedEdge(builder, method, annotation);
                else
                    return this.addVertexByObjectTypedEdge(builder, method, annotation);
            } else
                throw new IllegalStateException(method.getName() + " was annotated with @Adjacency but had more than 1 arguments.");
        else if (ReflectionUtility.isGetMethod(method))
            if (arguments == null || arguments.length == 0) {
                if (ReflectionUtility.returnsIterator(method))
                    return this.getVertexesIteratorDefault(builder, method, annotation);
                else if (ReflectionUtility.returnsList(method))
                    return this.getVertexesListDefault(builder, method, annotation);
                else if (ReflectionUtility.returnsSet(method))
                    return this.getVertexesSetDefault(builder, method, annotation);

                return this.getVertexDefault(builder, method, annotation);
            } else if (arguments.length == 1) {
                if (!(Class.class.isAssignableFrom(arguments[0].getType())))
                    throw new IllegalStateException(method.getName() + " was annotated with @Adjacency, had a single argument, but that argument was not of the type Class");

                if (ReflectionUtility.returnsIterator(method))
                    return this.getVertexesIteratorByType(builder, method, annotation);
                else if (ReflectionUtility.returnsList(method))
                    return this.getVertexesListByType(builder, method, annotation);
                else if (ReflectionUtility.returnsSet(method))
                    return this.getVertexesSetByType(builder, method, annotation);

                return this.getVertexByType(builder, method, annotation);
            } else
                throw new IllegalStateException(method.getName() + " was annotated with @Adjacency but had more than 1 arguments.");
        else if (ReflectionUtility.isRemoveMethod(method))
            if (arguments == null || arguments.length == 0)
                return this.removeAll(builder, method, annotation);
            else if (arguments.length == 1)
                return this.removeVertex(builder, method, annotation);
            else
                throw new IllegalStateException(method.getName() + " was annotated with @Adjacency but had more than 1 arguments.");
        else if (ReflectionUtility.isSetMethod(method))
            if (arguments == null || arguments.length == 0)
                throw new IllegalStateException(method.getName() + " was annotated with @Adjacency but had no arguments.");
            else if (arguments.length == 1) {
                if (ReflectionUtility.acceptsIterator(method, 0))
                    return this.setVertexIterator(builder, method, annotation);
                else if (ReflectionUtility.acceptsIterable(method, 0))
                    return this.setVertexIterable(builder, method, annotation);
                else if (ReflectionUtility.acceptsVertexFrame(method, 0))
                    return this.setVertexVertexFrame(builder, method, annotation);

                throw new IllegalStateException(method.getName() + " was annotated with @Adjacency, had a single argument, but that argument was not of the type Iterator or Iterable");
            } else
                throw new IllegalStateException(method.getName() + " was annotated with @Adjacency but had more than 1 arguments.");
        else
            throw new IllegalStateException(method.getName() + " was annotated with @Adjacency but did not begin with either of the following keywords: add, get, remove");
    }

    private <E> DynamicType.Builder<E> getVertexesIteratorDefault(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(GetVertexesIteratorDefaultInterceptor.class));
    }

    private <E> DynamicType.Builder<E> getVertexesListDefault(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(GetVertexesListDefaultInterceptor.class));
    }

    private <E> DynamicType.Builder<E> getVertexesSetDefault(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(GetVertexesSetDefaultInterceptor.class));
    }

    private <E> DynamicType.Builder<E> getVertexDefault(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(GetVertexDefaultInterceptor.class));
    }

    private <E> DynamicType.Builder<E> getVertexesIteratorByType(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(GetVertexesIteratorByTypeInterceptor.class));
    }

    private <E> DynamicType.Builder<E> getVertexesListByType(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(GetVertexesListByTypeInterceptor.class));
    }

    private <E> DynamicType.Builder<E> getVertexesSetByType(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(GetVertexesSetByTypeInterceptor.class));
    }

    private <E> DynamicType.Builder<E> getVertexByType(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(GetVertexByTypeInterceptor.class));
    }

    private <E> DynamicType.Builder<E> addVertexDefault(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(AddVertexDefaultInterceptor.class));
    }

    private <E> DynamicType.Builder<E> addVertexByTypeUntypedEdge(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(AddVertexByTypeUntypedEdgeInterceptor.class));
    }

    private <E> DynamicType.Builder<E> addVertexByObjectUntypedEdge(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(AddVertexByObjectUntypedEdgeInterceptor.class));
    }

    private <E> DynamicType.Builder<E> addVertexByTypeTypedEdge(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(AddVertexByTypeTypedEdgeInterceptor.class));
    }

    private <E> DynamicType.Builder<E> addVertexByObjectTypedEdge(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(AddVertexByObjectTypedEdgeInterceptor.class));
    }

    private <E> DynamicType.Builder<E> setVertexIterator(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(SetVertexIteratorInterceptor.class));
    }

    private <E> DynamicType.Builder<E> setVertexIterable(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(SetVertexIterableInterceptor.class));
    }

    private <E> DynamicType.Builder<E> setVertexVertexFrame(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(SetVertexVertexFrameInterceptor.class));
    }

    private <E> DynamicType.Builder<E> removeVertex(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(RemoveVertexInterceptor.class));
    }

    private <E> DynamicType.Builder<E> removeAll(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(RemoveAllInterceptor.class));
    }

    public static final class GetVertexesIteratorDefaultInterceptor {

        @RuntimeType
        public static Iterator getVertexes(@This final VertexFrame thiz, @Origin final Method method) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();

            return thiz.traverse(input -> {
                switch (direction) {
                    case IN:
                        return input.in(label);
                    case OUT:
                        return input.out(label);
                    case BOTH:
                        return input.both(label);
                    default:
                        throw new IllegalStateException("Direction not recognized.");
                }
            }).frame(VertexFrame.class);
        }
    }

    public static final class GetVertexesListDefaultInterceptor {

        @RuntimeType
        public static List getVertexes(@This final VertexFrame thiz, @Origin final Method method) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();

            return thiz.traverse(input -> {
                switch (direction) {
                    case IN:
                        return input.in(label);
                    case OUT:
                        return input.out(label);
                    case BOTH:
                        return input.both(label);
                    default:
                        throw new IllegalStateException("Direction not recognized.");
                }
            }).toList(VertexFrame.class);
        }
    }

    public static final class GetVertexesSetDefaultInterceptor {

        @RuntimeType
        public static Set getVertexes(@This final VertexFrame thiz, @Origin final Method method) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();

            return thiz.traverse(input -> {
                switch (direction) {
                    case IN:
                        return input.in(label);
                    case OUT:
                        return input.out(label);
                    case BOTH:
                        return input.both(label);
                    default:
                        throw new IllegalStateException("Direction not recognized.");
                }
            }).toSet(VertexFrame.class);
        }
    }

    public static final class GetVertexesIteratorByTypeInterceptor {

        @RuntimeType
        public static Iterator getVertexes(@This final VertexFrame thiz, @Origin final Method method, @RuntimeType @Argument(0) final Class type) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();
            final TypeResolver resolver = thiz.getGraph().getTypeResolver();

            return thiz.traverse(input -> {
                switch (direction) {
                    case IN:
                        return resolver.hasType(input.in(label), type);
                    case OUT:
                        return resolver.hasType(input.out(label), type);
                    case BOTH:
                        return resolver.hasType(input.both(label), type);
                    default:
                        throw new IllegalStateException("Direction not recognized.");
                }
            }).frame(type);
        }
    }

    public static final class GetVertexesListByTypeInterceptor {

        @RuntimeType
        public static List getVertexes(@This final VertexFrame thiz, @Origin final Method method, @RuntimeType @Argument(0) final Class type) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();
            final TypeResolver resolver = thiz.getGraph().getTypeResolver();

            return thiz.traverse(input -> {
                switch (direction) {
                    case IN:
                        return resolver.hasType(input.in(label), type);
                    case OUT:
                        return resolver.hasType(input.out(label), type);
                    case BOTH:
                        return resolver.hasType(input.both(label), type);
                    default:
                        throw new IllegalStateException("Direction not recognized.");
                }
            }).toList(type);
        }
    }

    public static final class GetVertexesSetByTypeInterceptor {

        @RuntimeType
        public static Set getVertexes(@This final VertexFrame thiz, @Origin final Method method, @RuntimeType @Argument(0) final Class type) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();
            final TypeResolver resolver = thiz.getGraph().getTypeResolver();

            return thiz.traverse(input -> {
                switch (direction) {
                    case IN:
                        return resolver.hasType(input.in(label), type);
                    case OUT:
                        return resolver.hasType(input.out(label), type);
                    case BOTH:
                        return resolver.hasType(input.both(label), type);
                    default:
                        throw new IllegalStateException("Direction not recognized.");
                }
            }).toSet(type);
        }
    }

    public static final class GetVertexDefaultInterceptor {

        @RuntimeType
        public static Object getVertexes(@This final VertexFrame thiz, @Origin final Method method) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();

            try {
                return thiz.traverse(input -> {
                    switch (direction) {
                        case IN:
                            return input.in(label);
                        case OUT:
                            return input.out(label);
                        case BOTH:
                            return input.both(label);
                        default:
                            throw new IllegalStateException("Direction not recognized.");
                    }
                }).next(VertexFrame.class);
            } catch (NoSuchElementException e) {
                return null;
            }
        }
    }

    public static final class GetVertexByTypeInterceptor {

        @RuntimeType
        public static Object getVertex(@This final VertexFrame thiz, @Origin final Method method, @RuntimeType @Argument(0) final Class type) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();
            final TypeResolver resolver = thiz.getGraph().getTypeResolver();

            return thiz.traverse(input -> {
                switch (direction) {
                    case IN:
                        return resolver.hasType(input.in(label), type);
                    case OUT:
                        return resolver.hasType(input.out(label), type);
                    case BOTH:
                        return resolver.hasType(input.both(label), type);
                    default:
                        throw new IllegalStateException("Direction not recognized.");
                }
            }).next(type);
        }
    }

    public static final class AddVertexDefaultInterceptor {

        @RuntimeType
        public static Object addVertex(@This final VertexFrame thiz, @Origin final Method method) {
            final VertexFrame newVertex = thiz.getGraph().addFramedVertex();
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();

            switch (direction) {
                case BOTH:
                    thiz.getGraph().addFramedEdge(newVertex, thiz, label);
                    thiz.getGraph().addFramedEdge(thiz, newVertex, label);
                    break;
                case IN:
                    thiz.getGraph().addFramedEdge(newVertex, thiz, label);
                    break;
                case OUT:
                    thiz.getGraph().addFramedEdge(thiz, newVertex, label);
                    break;
                default:
                    throw new IllegalStateException(method.getName() + " is annotated with a direction other than BOTH, IN, or OUT.");
            }

            return newVertex;
        }
    }

    public static final class AddVertexByTypeUntypedEdgeInterceptor {
        @RuntimeType
        public static Object addVertex(@This final VertexFrame thiz, @Origin final Method method, @RuntimeType @Argument(value = 0) final ClassInitializer vertexType) {
            final Object newNode = thiz.getGraph().addFramedVertex(vertexType);
            assert newNode instanceof VertexFrame;
            final VertexFrame newVertex = ((VertexFrame) newNode);

            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();

            assert vertexType.getInitializationType().isInstance(newNode);

            switch (direction) {
                case BOTH:
                    thiz.getGraph().addFramedEdge(newVertex, thiz, label);
                    thiz.getGraph().addFramedEdge(thiz, newVertex, label);
                    break;
                case IN:
                    thiz.getGraph().addFramedEdge(newVertex, thiz, label);
                    break;
                case OUT:
                    thiz.getGraph().addFramedEdge(thiz, newVertex, label);
                    break;
                default:
                    throw new IllegalStateException(method.getName() + " is annotated with a direction other than BOTH, IN, or OUT.");
            }

            return newNode;
        }
    }

    public static final class AddVertexByTypeTypedEdgeInterceptor {
        @RuntimeType
        public static Object addVertex(@This final VertexFrame thiz, @Origin final Method method, @RuntimeType @Argument(0) final ClassInitializer vertexType, @RuntimeType @Argument(1) final ClassInitializer edgeType) {
            final Object newNode = thiz.getGraph().addFramedVertex(vertexType);
            assert newNode instanceof VertexFrame;
            final VertexFrame newVertex = ((VertexFrame) newNode);

            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();

            assert vertexType.getInitializationType().isInstance(newNode);

            switch (direction) {
                case BOTH:
                    thiz.getGraph().addFramedEdge(newVertex, thiz, label, edgeType);
                    thiz.getGraph().addFramedEdge(thiz, newVertex, label, edgeType);
                    break;
                case IN:
                    thiz.getGraph().addFramedEdge(newVertex, thiz, label, edgeType);
                    break;
                case OUT:
                    thiz.getGraph().addFramedEdge(thiz, newVertex, label, edgeType);
                    break;
                default:
                    throw new IllegalStateException(method.getName() + " is annotated with a direction other than BOTH, IN, or OUT.");
            }

            return newNode;
        }
    }

    public static final class AddVertexByObjectUntypedEdgeInterceptor {

        @RuntimeType
        public static Object addVertex(@This final VertexFrame thiz, @Origin final Method method, @RuntimeType @Argument(0) final VertexFrame newVertex) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();

            switch (direction) {
                case BOTH:
                    thiz.getGraph().addFramedEdge(newVertex, thiz, label);
                    thiz.getGraph().addFramedEdge(thiz, newVertex, label);
                    break;
                case IN:
                    thiz.getGraph().addFramedEdge(newVertex, thiz, label);
                    break;
                case OUT:
                    thiz.getGraph().addFramedEdge(thiz, newVertex, label);
                    break;
                default:
                    throw new IllegalStateException(method.getName() + " is annotated with a direction other than BOTH, IN, or OUT.");
            }

            return newVertex;
        }
    }

    public static final class AddVertexByObjectTypedEdgeInterceptor {

        @RuntimeType
        public static Object addVertex(@This final VertexFrame thiz, @Origin final Method method, @RuntimeType @Argument(0) final VertexFrame newVertex, @RuntimeType @Argument(1) final ClassInitializer edgeType) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();

            switch (direction) {
                case BOTH:
                    thiz.getGraph().addFramedEdge(newVertex, thiz, label, edgeType);
                    thiz.getGraph().addFramedEdge(thiz, newVertex, label, edgeType);
                    break;
                case IN:
                    thiz.getGraph().addFramedEdge(newVertex, thiz, label, edgeType);
                    break;
                case OUT:
                    thiz.getGraph().addFramedEdge(thiz, newVertex, label, edgeType);
                    break;
                default:
                    throw new IllegalStateException(method.getName() + " is annotated with a direction other than BOTH, IN, or OUT.");
            }

            return newVertex;
        }
    }

    public static final class SetVertexIteratorInterceptor {

        @RuntimeType
        public static void setVertex(@This final VertexFrame thiz, @Origin final Method method, @RuntimeType @Argument(0) final Iterator vertexSet) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();


            switch (direction) {
                case BOTH:
                    thiz.unlinkBoth(null, label);
                    ((Iterator<? extends VertexFrame>) vertexSet).forEachRemaining(new Consumer<VertexFrame>() {
                        @Override
                        public void accept(VertexFrame vertexFrame) {
                            thiz.getGraph().addFramedEdge(vertexFrame, thiz, label);
                            thiz.getGraph().addFramedEdge(thiz, vertexFrame, label);
                        }
                    });
                    break;
                case IN:
                    thiz.unlinkIn(null, label);
                    ((Iterator<? extends VertexFrame>) vertexSet).forEachRemaining(new Consumer<VertexFrame>() {
                        @Override
                        public void accept(VertexFrame vertexFrame) {
                            thiz.getGraph().addFramedEdge(vertexFrame, thiz, label);
                        }
                    });
                    break;
                case OUT:
                    thiz.unlinkOut(null, label);
                    ((Iterator<? extends VertexFrame>) vertexSet).forEachRemaining(new Consumer<VertexFrame>() {
                        @Override
                        public void accept(VertexFrame vertexFrame) {
                            thiz.getGraph().addFramedEdge(thiz, vertexFrame, label);
                        }
                    });
                    break;
                default:
                    throw new IllegalStateException(method.getName() + " is annotated with a direction other than BOTH, IN, or OUT.");
            }
        }
    }

    public static final class SetVertexIterableInterceptor {

        @RuntimeType
        public static void setVertex(@This final VertexFrame thiz, @Origin final Method method, @RuntimeType @Argument(0) final Iterable vertexSet) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();


            switch (direction) {
                case BOTH:
                    thiz.unlinkBoth(null, label);
                    ((Iterator<? extends VertexFrame>) vertexSet.iterator()).forEachRemaining(new Consumer<VertexFrame>() {
                        @Override
                        public void accept(VertexFrame vertexFrame) {
                            thiz.getGraph().addFramedEdge(vertexFrame, thiz, label);
                            thiz.getGraph().addFramedEdge(thiz, vertexFrame, label);
                        }
                    });
                    break;
                case IN:
                    thiz.unlinkIn(null, label);
                    ((Iterator<? extends VertexFrame>) vertexSet.iterator()).forEachRemaining(new Consumer<VertexFrame>() {
                        @Override
                        public void accept(VertexFrame vertexFrame) {
                            thiz.getGraph().addFramedEdge(vertexFrame, thiz, label);
                        }
                    });
                    break;
                case OUT:
                    thiz.unlinkOut(null, label);
                    ((Iterator<? extends VertexFrame>) vertexSet.iterator()).forEachRemaining(new Consumer<VertexFrame>() {
                        @Override
                        public void accept(VertexFrame vertexFrame) {
                            thiz.getGraph().addFramedEdge(thiz, vertexFrame, label);
                        }
                    });
                    break;
                default:
                    throw new IllegalStateException(method.getName() + " is annotated with a direction other than BOTH, IN, or OUT.");
            }
        }
    }

    public static final class SetVertexVertexFrameInterceptor {

        @RuntimeType
        public static void setVertex(@This final VertexFrame thiz, @Origin final Method method, @RuntimeType @Argument(0) final VertexFrame vertexFrame) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();
            if (vertexFrame == null) {
                RemoveAllInterceptor.removeVertex(thiz, method);
                return;
            }


            switch (direction) {
                case BOTH:
                    thiz.unlinkBoth(null, label);
                    thiz.getGraph().addFramedEdge(vertexFrame, thiz, label);
                    thiz.getGraph().addFramedEdge(thiz, vertexFrame, label);
                    break;
                case IN:
                    thiz.unlinkIn(null, label);
                    thiz.getGraph().addFramedEdge(vertexFrame, thiz, label);
                    break;
                case OUT:
                    thiz.unlinkOut(null, label);
                    thiz.getGraph().addFramedEdge(thiz, vertexFrame, label);
                    break;
                default:
                    throw new IllegalStateException(method.getName() + " is annotated with a direction other than BOTH, IN, or OUT.");
            }
        }
    }

    public static final class RemoveVertexInterceptor {

        @RuntimeType
        public static void removeVertex(@This final VertexFrame thiz, @Origin final Method method, @RuntimeType @Argument(0) final VertexFrame removeVertex) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();

            switch (direction) {
                case BOTH:
                    final Iterator<Edge> bothEdges = thiz.getRawTraversal().bothE(label);
                    bothEdges.forEachRemaining(new Consumer<Edge>() {
                        @Override
                        public void accept(final Edge edge) {
                            if (null == removeVertex || edge.outVertex().id().equals(removeVertex.getId()) || edge.inVertex().id().equals(removeVertex.getId()))
                                edge.remove();
                        }
                    });
                    break;
                case IN:
                    final Iterator<Edge> inEdges = thiz.getRawTraversal().inE(label);
                    inEdges.forEachRemaining(new Consumer<Edge>() {
                        @Override
                        public void accept(final Edge edge) {
                            if (null == removeVertex || edge.outVertex().id().equals(removeVertex.getId()))
                                edge.remove();
                        }
                    });
                    break;
                case OUT:
                    final Iterator<Edge> outEdges = thiz.getRawTraversal().outE(label);
                    outEdges.forEachRemaining(new Consumer<Edge>() {
                        @Override
                        public void accept(final Edge edge) {
                            if (null == removeVertex || edge.inVertex().id().equals(removeVertex.getId()))
                                edge.remove();
                        }
                    });
                    break;
                default:
                    throw new IllegalStateException(method.getName() + " is annotated with a direction other than BOTH, IN, or OUT.");
            }
        }
    }

    public static final class RemoveAllInterceptor {

        @RuntimeType
        public static void removeVertex(@This final VertexFrame thiz, @Origin final Method method) {
            assert thiz instanceof CachesReflection;
            final Adjacency annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Adjacency.class);
            final Direction direction = annotation.direction();
            final String label = annotation.label();

            switch (direction) {
                case BOTH:
                    final Iterator<Edge> bothEdges = thiz.getRawTraversal().bothE(label);
                    bothEdges.forEachRemaining(new Consumer<Edge>() {
                        @Override
                        public void accept(final Edge edge) {
                            edge.remove();
                        }
                    });
                    break;
                case IN:
                    final Iterator<Edge> inEdges = thiz.getRawTraversal().inE(label);
                    inEdges.forEachRemaining(new Consumer<Edge>() {
                        @Override
                        public void accept(final Edge edge) {
                            edge.remove();
                        }
                    });
                    break;
                case OUT:
                    final Iterator<Edge> outEdges = thiz.getRawTraversal().outE(label);
                    outEdges.forEachRemaining(new Consumer<Edge>() {
                        @Override
                        public void accept(final Edge edge) {
                            edge.remove();
                        }
                    });
                    break;
                default:
                    throw new IllegalStateException(method.getName() + " is annotated with a direction other than BOTH, IN, or OUT.");
            }
        }
    }
}

