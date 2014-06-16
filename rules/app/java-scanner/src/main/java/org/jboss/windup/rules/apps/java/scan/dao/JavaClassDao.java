package org.jboss.windup.rules.apps.java.scan.dao;

import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

public interface JavaClassDao extends BaseDao<JavaClassModel>
{

    public JavaClassModel getJavaClass(String qualifiedName);

    public JavaClassModel createJavaClass(String qualifiedName);

    public Iterable<JavaClassModel> findByJavaClassPattern(String regex);

    public Iterable<JavaClassModel> findByJavaPackage(String packageName);

    public Iterable<JavaClassModel> findByJavaVersion(JavaVersion version);

    public Iterable<JavaClassModel> getAllClassNotFound();

    public Iterable<JavaClassModel> getAllDuplicateClasses();

    public boolean isJavaClass(ResourceModel resource);

    public JavaClassModel getJavaClassFromResource(ResourceModel resource);

    public void markAsBlacklistCandidate(JavaClassModel clz);

    public void markAsCustomerPackage(JavaClassModel clz);

    public Iterable<JavaClassModel> findClassesWithSource();

    public Iterable<JavaClassModel> findCandidateBlacklistClasses();

    public Iterable<JavaClassModel> findClassesLeveragingCandidateBlacklists();

    public Iterable<JavaClassModel> findLeveragedCandidateBlacklists(JavaClassModel clz);

    public Iterable<JavaClassModel> findCustomerPackageClasses();

    public enum JavaVersion
    {
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
