package org.jboss.windup.rules.apps.java.service;

import java.util.HashSet;
import java.util.Set;

import org.jboss.forge.roaster.model.util.Types;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.exception.NonUniqueResultException;
import org.jboss.windup.rules.apps.java.model.AmbiguousJavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.jboss.windup.rules.apps.java.model.JavaParameterModel;
import org.jboss.windup.rules.apps.java.model.PhantomJavaClassModel;
import org.jboss.windup.util.ExecutionStatistics;

/**
 * Contains methods for searching, updating, and deleting {@link JavaClassModel} frames.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class JavaClassService extends GraphService<JavaClassModel>
{
    public JavaClassService(GraphContext context)
    {
        super(context, JavaClassModel.class);
    }

    /**
     * Find a {@link JavaClassModel} by the qualified name, returning a single result. If more than one result is available, a
     * {@link AmbiguousJavaClassModel} reference will be returned.
     */
    public JavaClassModel getByName(String qualifiedName) throws NonUniqueResultException
    {
        ExecutionStatistics.get().begin("getUniqueByName(qualifiedName)");
        JavaClassModel result = resolveByQualifiedName(qualifiedName);
        ExecutionStatistics.get().end("getUniqueByName(qualifiedName)");
        return result;
    }

    /**
     * Indicates that we have found a .class or .java file for the given qualified name. This will either create a new {@link JavaClassModel} or
     * convert an existing {@link PhantomJavaClassModel} if one exists.
     */
    public synchronized JavaClassModel create(String qualifiedName)
    {
        // if a phantom exists, just convert it
        PhantomJavaClassModel phantom = new GraphService<>(getGraphContext(), PhantomJavaClassModel.class).getUniqueByProperty(
                    JavaClassModel.QUALIFIED_NAME, qualifiedName);
        if (phantom != null)
        {
            GraphService.removeTypeFromModel(getGraphContext(), phantom, PhantomJavaClassModel.class);
            return phantom;
        }
        JavaClassModel javaClassModel = super.create();
        setPropertiesFromName(javaClassModel, qualifiedName);
        return javaClassModel;
    }

    /**
     * Gets an existing {@link JavaClassModel}, however if none currently exists, then create a {@link PhantomJavaClassModel}.<br/>
     * 
     * This is intended to indicate that we know about a class by reference (for example another class subclasses it, or it is referenced in an XML
     * file), but we do not yet have a location on the disk for the class (or source) file itself.
     * 
     * To create a class (possibly converting a {@link PhantomJavaClassModel} to concrete in the process), use {@link JavaClassService#create(String)}
     * instead.
     */
    public synchronized JavaClassModel getOrCreatePhantom(String qualifiedName)
    {
        JavaClassModel result = resolveByQualifiedName(qualifiedName);
        if (result == null)
        {
            // create a phantom
            result = new GraphService<>(getGraphContext(), PhantomJavaClassModel.class).create();
            setPropertiesFromName(result, qualifiedName);
        }
        return result;
    }

    private void setPropertiesFromName(JavaClassModel model, String qualifiedName)
    {
        model.setQualifiedName(qualifiedName);
        model.setSimpleName(Types.toSimpleName(qualifiedName));
        model.setPackageName(Types.getPackage(qualifiedName));
    }

    public Iterable<JavaClassModel> findByJavaClassPattern(String regex)
    {
        ExecutionStatistics.get().begin("JavaClassService.findByJavaClassPattern(regex)");
        Iterable<JavaClassModel> result = super.findAllByPropertyMatchingRegex("qualifiedName", regex);
        ExecutionStatistics.get().end("JavaClassService.findByJavaClassPattern(regex)");
        return result;
    }

    public Iterable<JavaClassModel> findByJavaPackage(String packageName)
    {
        ExecutionStatistics.get().begin("JavaClassService.findByJavaPackage(packageName)");
        Iterable<JavaClassModel> result = getGraphContext().getQuery().type(JavaClassModel.class)
                    .has(JavaClassModel.PACKAGE_NAME, packageName).vertices(getType());
        ExecutionStatistics.get().end("JavaClassService.findByJavaPackage(packageName)");
        return result;

    }

    public Iterable<JavaClassModel> findByJavaVersion(JavaVersion version)
    {
        ExecutionStatistics.get().begin("JavaClassService.findByJavaVersion(version)");
        Iterable<JavaClassModel> result = getGraphContext().getQuery().type(JavaClassModel.class)
                    .has(JavaClassModel.MAJOR_VERSION, version.getMajor())
                    .has(JavaClassModel.MINOR_VERSION, version.getMinor()).vertices(getType());
        ExecutionStatistics.get().end("JavaClassService.findByJavaVersion(version)");
        return result;
    }

    /**
     * Since {@link JavaClassModel} may actually be ambiguous if multiple copies of the class have been defined, attempt to resolve the unique
     * instance, or return an {@link AmbiguousJavaClassModel} if multiple types exist.
     */
    private JavaClassModel resolveByQualifiedName(String qualifiedClassName)
    {
        ExecutionStatistics.get().begin("JavaClassService.resolveByQualifiedName(qualifiedClassName)");
        try
        {
            JavaClassModel model = getUniqueByProperty(JavaClassModel.QUALIFIED_NAME,
                        qualifiedClassName);
            return model;
        }
        catch (NonUniqueResultException e)
        {
            Iterable<JavaClassModel> candidates = findAllByProperty(
                        JavaClassModel.QUALIFIED_NAME, qualifiedClassName);

            AmbiguousJavaClassModel ambiguousModel = null;
            for (JavaClassModel candidate : candidates)
            {
                if (candidate instanceof AmbiguousJavaClassModel)
                    ambiguousModel = (AmbiguousJavaClassModel) candidate;
            }

            if (ambiguousModel == null)
            {
                GraphService<AmbiguousJavaClassModel> ambiguousJavaClassModelService = new GraphService<>(
                            getGraphContext(), AmbiguousJavaClassModel.class);
                ambiguousModel = ambiguousJavaClassModelService.create();
            }

            Set<JavaClassModel> existingAmbiguousEntries = new HashSet<>();
            for (JavaClassModel existingAmbiguousClass : ambiguousModel.getReferences())
            {
                existingAmbiguousEntries.add(existingAmbiguousClass);
            }

            for (JavaClassModel candidate : candidates)
            {
                if (!existingAmbiguousEntries.contains(candidate))
                    ambiguousModel.addReference(candidate);
            }
            return ambiguousModel;
        }
        finally
        {
            ExecutionStatistics.get().end("JavaClassService.resolveByQualifiedName(qualifiedClassName)");
        }
    }

    public JavaMethodModel addJavaMethod(JavaClassModel jcm, String methodName, JavaClassModel[] params)
    {
        ExecutionStatistics.get().begin("JavaClassService.addJavaMethod(jcm, methodName, params)");
        JavaMethodModel javaMethodModel = getGraphContext().getFramed().addVertex(null, JavaMethodModel.class);
        javaMethodModel.setMethodName(methodName);

        for (int i = 0; i < params.length; i++)
        {
            JavaClassModel param = params[i];
            JavaParameterModel paramModel = getGraphContext().getFramed().addVertex(null, JavaParameterModel.class);
            paramModel.setJavaType(param);
            paramModel.setPosition(i);
            javaMethodModel.addMethodParameter(paramModel);
        }
        jcm.addJavaMethod(javaMethodModel);
        ExecutionStatistics.get().end("JavaClassService.addJavaMethod(jcm, methodName, params)");
        return javaMethodModel;
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
