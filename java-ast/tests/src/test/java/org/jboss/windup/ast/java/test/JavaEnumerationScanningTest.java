package org.jboss.windup.ast.java.test;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.ast.java.ASTProcessor;
import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JavaEnumerationScanningTest extends AbstractJavaASTTest {

    @Test
    public void testEnumerationUsage() {
        List<ClassReference> references = ASTProcessor.analyze(getLibraryPaths(), getSourcePaths(),
                Paths.get("src/test/resources/testclasses/enumeration/EnumerationClassUsage.java"));
        int counter = 0;
        for (ClassReference reference : references) {
            if (reference.getQualifiedName().startsWith("java.nio.file.AccessMode") && reference.getLocation() == TypeReferenceLocation.ENUM_CONSTANT) {
                System.out.println("Reference: " + reference);
                counter++;
            }
        }
        Assert.assertEquals(3, counter);

        ClassReference referenceTest = new ClassReference("java.nio.file.AccessMode.WRITE", "java.nio.file", "AccessMode", null,
                ResolutionStatus.RESOLVED, TypeReferenceLocation.ENUM_CONSTANT, 12, 4, 39, "AccessMode testMode=AccessMode.WRITE;");
        Assert.assertTrue(references.contains(referenceTest));

        referenceTest = new ClassReference("java.nio.file.AccessMode.WRITE", "java.nio.file", "AccessMode", null,
                ResolutionStatus.RESOLVED, TypeReferenceLocation.ENUM_CONSTANT, 17, 30, 18, "EnumerationClassUsage.testAccessibleEnum(AccessMode.WRITE)");
        Assert.assertTrue(references.contains(referenceTest));

        referenceTest = new ClassReference("java.nio.file.AccessMode.READ", "java.nio.file", "AccessMode", null,
                ResolutionStatus.RESOLVED, TypeReferenceLocation.ENUM_CONSTANT, 31, 41, 21, "new EnumerationClassUsage(AccessMode.READ)");
        Assert.assertTrue(references.contains(referenceTest));
    }

    @Test
    public void testEnumConstWithoutClassOnClasspath() {
        Set<String> libraryPaths = new HashSet<>();
        libraryPaths.add("src/test/resources/testclasses/enumeration/dependency/hibernate-search-engine-5.5.3.Final.jar");

        List<ClassReference> references = ASTProcessor.analyze(libraryPaths, getSourcePaths(),
                Paths.get("src/test/resources/testclasses/enumeration/EnumConstClassNotOnClasspath.java"));
        int counter = 0;
        for (ClassReference reference : references) {
            if (reference.getQualifiedName().contains(".IndexWriterSetting.") && reference.getLocation() == TypeReferenceLocation.ENUM_CONSTANT) {
                System.out.println("Reference: " + reference);
                counter++;

            }
        }
        Assert.assertEquals(4, counter);

        ClassReference referenceTest = new ClassReference("org.hibernate.search.backend.configuration.impl.IndexWriterSetting.MAX_THREAD_STATES",
                "org.hibernate.search.backend.configuration.impl", "IndexWriterSetting", null,
                ResolutionStatus.RESOLVED, TypeReferenceLocation.ENUM_CONSTANT, 13, 4, 72,
                "IndexWriterSetting writerSetting=IndexWriterSetting.MAX_THREAD_STATES;");
        Assert.assertTrue(references.contains(referenceTest));

        referenceTest = new ClassReference("org.hibernate.search.backend.configuration.impl.IndexWriterSetting.MAX_THREAD_STATES",
                "org.hibernate.search.backend.configuration.impl", "IndexWriterSetting", null,
                ResolutionStatus.RESOLVED, TypeReferenceLocation.ENUM_CONSTANT, 15, 4, 54,
                "IndexWriterSetting writerSetting1=MAX_THREAD_STATES;");
        Assert.assertTrue(references.contains(referenceTest));

        referenceTest = new ClassReference("org.hibernate.search.backend.configuration.impl.IndexWriterSetting.TERM_INDEX_INTERVAL",
                "org.hibernate.search.backend.configuration.impl", "IndexWriterSetting", null,
                ResolutionStatus.RESOLVED, TypeReferenceLocation.ENUM_CONSTANT, 20, 30, 8,
                "EnumerationClassUsage.testEnum(IndexWriterSetting.TERM_INDEX_INTERVAL)");
        Assert.assertTrue(references.contains(referenceTest));

        referenceTest = new ClassReference("org.hibernate.search.backend.configuration.impl.IndexWriterSetting.TERM_INDEX_INTERVAL",
                "org.hibernate.search.backend.configuration.impl", "IndexWriterSetting", null,
                ResolutionStatus.RESOLVED, TypeReferenceLocation.ENUM_CONSTANT, 34, 48, 28,
                "new EnumConstClassNotOnClasspath(IndexWriterSetting.TERM_INDEX_INTERVAL)");
        Assert.assertTrue(references.contains(referenceTest));
    }

}
