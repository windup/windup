package org.jboss.windup.rules.apps.java.service;

import org.jboss.forge.roaster.model.util.Types;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.exception.NonUniqueResultException;
import org.jboss.windup.rules.apps.java.model.AmbiguousJavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.jboss.windup.rules.apps.java.model.JavaParameterModel;
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
     * Find a {@link JavaClassModel} by the qualified name
     */
    public JavaClassModel getUniqueByName(String qualifiedName) throws NonUniqueResultException
    {
        ExecutionStatistics.get().begin("getUniqueByName(qualifiedName)");
        JavaClassModel result = getUniqueByProperty(JavaClassModel.PROPERTY_QUALIFIED_NAME, qualifiedName);
        ExecutionStatistics.get().end("getUniqueByName(qualifiedName)");
        return result;
    }

    public synchronized JavaClassModel getOrCreate(String qualifiedName) throws NonUniqueResultException
    {
        ExecutionStatistics.get().begin("JavaClassService.getOrCreate(qualifiedName)");
        JavaClassModel clz = resolveByQualifiedName(qualifiedName);

        if (clz == null)
        {
            clz = (JavaClassModel) this.create();
            clz.setQualifiedName(qualifiedName);
            clz.setSimpleName(Types.toSimpleName(qualifiedName));
            clz.setPackageName(Types.getPackage(qualifiedName));
        }

        ExecutionStatistics.get().end("JavaClassService.getOrCreate(qualifiedName)");
        return clz;
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
    public JavaClassModel resolveByQualifiedName(String qualifiedClassName)
    {
        ExecutionStatistics.get().begin("JavaClassService.resolveByQualifiedName(qualifiedClassName)");
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
