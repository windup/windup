package org.jboss.windup.rules.apps.java.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.forge.roaster.model.util.Types;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.exception.NonUniqueResultException;
import org.jboss.windup.rules.apps.java.model.AmbiguousJavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.jboss.windup.rules.apps.java.model.JavaParameterModel;

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
        return getUniqueByProperty(JavaClassModel.PROPERTY_QUALIFIED_NAME, qualifiedName);
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
        return getGraphContext().getQuery().type(JavaClassModel.class)
                    .has("packageName", packageName).vertices(getType());
    }

    public Iterable<JavaClassModel> findByJavaVersion(JavaVersion version)
    {
        return getGraphContext().getQuery().type(JavaClassModel.class)
                    .has(JavaClassModel.MAJOR_VERSION, version.getMajor())
                    .has(JavaClassModel.MINOR_VERSION, version.getMinor()).vertices(getType());
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

    public JavaMethodModel addJavaMethod(JavaClassModel jcm, String methodName, JavaClassModel[] params)
    {
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
