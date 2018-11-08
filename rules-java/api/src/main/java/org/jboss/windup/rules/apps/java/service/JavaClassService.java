package org.jboss.windup.rules.apps.java.service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jboss.forge.roaster.model.util.Types;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.exception.NonUniqueResultException;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.AmbiguousJavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.jboss.windup.rules.apps.java.model.JavaParameterModel;
import org.jboss.windup.rules.apps.java.model.PhantomJavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.util.ExecutionStatistics;

/**
 * Contains methods for searching, updating, and deleting {@link JavaClassModel} frames.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
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
        List<JavaClassModel> result = (List<JavaClassModel>)getGraphContext().getQuery(JavaClassModel.class)
                .traverse(g -> g.has(JavaClassModel.PACKAGE_NAME, packageName))
                .toList(JavaClassModel.class);
        ExecutionStatistics.get().end("JavaClassService.findByJavaPackage(packageName)");
        return result;

    }

    public Iterable<JavaClassModel> findByJavaVersion(JavaVersion version)
    {
        ExecutionStatistics.get().begin("JavaClassService.findByJavaVersion(version)");
        List<JavaClassModel> result = (List<JavaClassModel>)getGraphContext().getQuery(JavaClassModel.class)
                .traverse(g -> g.has(JavaClassModel.MAJOR_VERSION, version.getMajor()))
                .traverse(g -> g.has(JavaClassModel.MINOR_VERSION, version.getMinor()))
                .toList(JavaClassModel.class);
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

    /**
     * This simply adds the interface to the provided {@link JavaClassModel} while checking for duplicate entries.
     */
    public void addInterface(JavaClassModel jcm, JavaClassModel interfaceJCM)
    {
        for (JavaClassModel existingInterface : jcm.getInterfaces())
        {
            if (existingInterface.equals(interfaceJCM))
                return;
        }

        jcm.addInterface(interfaceJCM);
    }

    public JavaMethodModel addJavaMethod(JavaClassModel jcm, String methodName, JavaClassModel[] params)
    {
        ExecutionStatistics.get().begin("JavaClassService.addJavaMethod(jcm, methodName, params)");
        JavaMethodModel javaMethodModel = getGraphContext().getFramed().addFramedVertex(JavaMethodModel.class);
        javaMethodModel.setMethodName(methodName);

        for (int i = 0; i < params.length; i++)
        {
            JavaClassModel param = params[i];
            JavaParameterModel paramModel = getGraphContext().getFramed().addFramedVertex(JavaParameterModel.class);
            paramModel.setJavaType(param);
            paramModel.setPosition(i);
            javaMethodModel.addMethodParameter(paramModel);
        }
        jcm.addJavaMethod(javaMethodModel);
        ExecutionStatistics.get().end("JavaClassService.addJavaMethod(jcm, methodName, params)");
        return javaMethodModel;
    }
    
    public Iterable<AbstractJavaSourceModel> getJavaSource(String clz) {
        List<AbstractJavaSourceModel> sources = new LinkedList<>();
        
        JavaClassModel classModel = getByName(clz);
        if(classModel == null) {
            return sources;
        }
        
        if (classModel instanceof AmbiguousJavaClassModel)
        {
            AmbiguousJavaClassModel ambiguousJavaClassModel = (AmbiguousJavaClassModel) classModel;
            for (JavaClassModel referencedClass : ambiguousJavaClassModel.getReferences())
            {
                if(referencedClass.getDecompiledSource() != null) {
                    sources.add(referencedClass.getDecompiledSource());
                }
                if(referencedClass.getOriginalSource() != null) {
                    sources.add(referencedClass.getOriginalSource());
                }
            }
        }
        else
        {
            if(classModel.getDecompiledSource() != null) {
                sources.add(classModel.getDecompiledSource());
            }
            if(classModel.getOriginalSource() != null) {
                sources.add(classModel.getOriginalSource());
            }
        }
        return sources;
    }

    public JavaClassModel getJavaClass(JavaTypeReferenceModel javaTypeReference)
    {
        JavaClassModel result = null;
        AbstractJavaSourceModel javaSource = javaTypeReference.getFile();
        for (JavaClassModel javaClassModel : javaSource.getJavaClasses())
        {
            // there can be only one public one, and the annotated class should be public
            if (javaClassModel.isPublic() != null && javaClassModel.isPublic())
            {
                result = javaClassModel;
                break;
            }
        }

        if (result == null)
        {
            // no public classes found, so try to find any class (even non-public ones)
            result = javaSource.getJavaClasses().iterator().next();
        }
        return result;
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
