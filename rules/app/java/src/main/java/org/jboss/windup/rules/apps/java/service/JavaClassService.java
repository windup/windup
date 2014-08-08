package org.jboss.windup.rules.apps.java.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.forge.roaster.model.util.Types;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.exception.NonUniqueResultException;
import org.jboss.windup.rules.apps.java.model.AmbiguousJavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

public class JavaClassService extends GraphService<JavaClassModel>
{
    @Inject
    public JavaClassService(GraphContext context)
    {
        super(context, JavaClassModel.class);
    }

    public JavaClassModel getUniqueByName(String qualifiedName) throws NonUniqueResultException
    {
        return getUniqueByProperty("qualifiedName", qualifiedName);
    }

    public synchronized JavaClassModel getOrCreate(String qualifiedName) throws NonUniqueResultException
    {
        JavaClassModel clz = resolveByQualifiedName(qualifiedName);

        if (clz == null)
        {
            clz = (JavaClassModel) this.create();
            clz.setQualifiedName(qualifiedName);
            clz.setSimpleName(Types.toSimpleName(qualifiedName));
            clz.setPackageName(Types.getPackage(qualifiedName));
        }

        return clz;
    }

    public Iterable<JavaClassModel> findByJavaClassPattern(String regex)
    {
        return super.findAllByPropertyMatchingRegex("qualifiedName", regex);
    }

    public Iterable<JavaClassModel> findByJavaPackage(String packageName)
    {
        return getGraphContext().getFramed().query()
                    .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, getTypeValueForSearch())
                    .has("packageName", packageName).vertices(getType());
    }

    public Iterable<JavaClassModel> findByJavaVersion(JavaVersion version)
    {
        return getGraphContext().getFramed().query()
                    .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, getTypeValueForSearch())
                    .has("majorVersion", version.getMajor())
                    .has("minorVersion", version.getMinor()).vertices(getType());
    }

    public Iterable<JavaClassModel> getAllClassNotFound()
    {
        // iterate through all vertices
        Iterable<Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(getTypedQuery().vertices())

                    // check to see whether there is an edge coming in that links to the resource providing the java
                    // class model.
                    .filter(new PipeFunction<Vertex, Boolean>()
                    {
                        public Boolean compute(Vertex argument)
                        {
                            if (argument.getEdges(Direction.IN, "javaClassFacet").iterator().hasNext())
                            {
                                return false;
                            }
                            // allow it through if there are no edges coming in that provide the java class model.
                            return true;
                        }
                    });
        return getGraphContext().getFramed().frameVertices(pipeline, JavaClassModel.class);
    }

    public Iterable<JavaClassModel> getAllDuplicateClasses()
    {
        // iterate through all vertices
        Iterable<Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(getTypedQuery().vertices())

                    // check to see whether there is an edge coming in that links to the resource providing the java
                    // class model.
                    .filter(new PipeFunction<Vertex, Boolean>()
                    {
                        public Boolean compute(Vertex argument)
                        {
                            Iterator<Edge> edges = argument.getEdges(Direction.IN, "javaClassFacet").iterator();
                            if (edges.hasNext())
                            {
                                edges.next();
                                if (edges.hasNext())
                                {
                                    return true;
                                }
                            }
                            // if there aren't two edges, return false.
                            return false;
                        }
                    });
        return getGraphContext().getFramed().frameVertices(pipeline, JavaClassModel.class);
    }

    public JavaClassModel getJavaClassFromResource(ResourceModel resource)
    {
        Iterator<Vertex> v = (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("javaClassFacet")
                    .iterator();
        if (v.hasNext())
        {
            return getGraphContext().getFramed().frame(v.next(), JavaClassModel.class);
        }

        return null;
    }

    public Iterable<JavaClassModel> findClassesWithSource()
    {
        // iterate through all vertices
        Iterable<Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(getTypedQuery().vertices())

                    // check to see whether there is an edge coming in that links to the resource providing the java
                    // class model.
                    .filter(new PipeFunction<Vertex, Boolean>()
                    {
                        public Boolean compute(Vertex argument)
                        {
                            return argument.getEdges(Direction.OUT, "source").iterator().hasNext();
                        }
                    });
        return getGraphContext().getFramed().frameVertices(pipeline, JavaClassModel.class);
    }

    public Iterable<JavaClassModel> findCandidateBlacklistClasses()
    {
        Iterable<Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(getTypedQuery().vertices())
                    .as("clz").has("blacklistCandidate").back("clz").cast(Vertex.class);
        return getGraphContext().getFramed().frameVertices(pipeline, JavaClassModel.class);
    }

    public Iterable<JavaClassModel> findClassesLeveragingCandidateBlacklists()
    {
        Iterable<Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(getTypedQuery().vertices())
                    .has("blacklistCandidate")
                    .in("extends", "imports", "implements")
                    .dedup();
        return getGraphContext().getFramed().frameVertices(pipeline, JavaClassModel.class);
    }

    public Iterable<JavaClassModel> findLeveragedCandidateBlacklists(JavaClassModel clz)
    {
        Set<JavaClassModel> results = new HashSet<JavaClassModel>();
        for (JavaClassModel javaClz : clz.dependsOnJavaClass())
        {
            if (javaClz.isBlacklistCandidate())
            {
                results.add(javaClz);
            }
        }

        return results;
    }

    public Iterable<JavaClassModel> findCustomerPackageClasses()
    {
        // for all java classes
        Iterable<Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(getTypedQuery().vertices())
                    .has("customerPackage").V();
        return getGraphContext().getFramed().frameVertices(pipeline, JavaClassModel.class);
    }

    /**
     * Since {@link JavaClassModel} may actually be ambiguous if multiple copies of the class have been defined, attempt
     * to resolve the unique instance, or return an {@link AmbiguousJavaClassModel} if multiple types exist.
     */
    public JavaClassModel resolveByQualifiedName(String qualifiedClassName)
    {
        try
        {
            JavaClassModel model = getUniqueByProperty(JavaClassModel.PROPERTY_QUALIFIED_NAME,
                        qualifiedClassName);

            return model;
        }
        catch (NonUniqueResultException e)
        {
            Iterable<JavaClassModel> candidates = findAllByProperty(
                        JavaClassModel.PROPERTY_QUALIFIED_NAME, qualifiedClassName);

            for (JavaClassModel candidate : candidates)
            {
                if (candidate instanceof AmbiguousJavaClassModel)
                    return candidate;
            }

            GraphService<AmbiguousJavaClassModel> ambiguousJavaClassModelService = new GraphService<>(
                        getGraphContext(), AmbiguousJavaClassModel.class);

            AmbiguousJavaClassModel ambiguousModel = ambiguousJavaClassModelService.create();
            for (JavaClassModel candidate : candidates)
            {
                ambiguousModel.setSimpleName(Types.toSimpleName(qualifiedClassName));
                ambiguousModel.setPackageName(Types.getPackage(qualifiedClassName));
                ambiguousModel.setQualifiedName(qualifiedClassName);
                ambiguousModel.addReference(candidate);
            }

            return ambiguousModel;
        }
    }

    public enum JavaVersion
    {
        JAVA_8(8, 0),
        JAVA_7(7, 0),
        JAVA_6(6, 0),
        JAVA_5(5, 0),
        JAVA_1_4(1, 4),
        JAVA_1_3(1, 3),
        JAVA_1_2(1, 2),
        JAVA_1_1(1, 1);

        final int major;
        final int minor;

        JavaVersion(int major, int minor)
        {
            this.major = major;
            this.minor = minor;
        }

        public int getMajor()
        {
            return major;
        }

        public int getMinor()
        {
            return minor;
        }
    }
}
