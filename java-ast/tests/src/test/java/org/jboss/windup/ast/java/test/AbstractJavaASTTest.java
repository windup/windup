package org.jboss.windup.ast.java.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

@RunWith(Arquillian.class)
public abstract class AbstractJavaASTTest {
    private Set<String> libraryPaths = new HashSet<>();
    private Set<String> sourcePaths;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.ast:windup-java-ast"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addPackage(AbstractJavaASTTest.class.getPackage());
        return archive;
    }

    @Before
    public void before() {
        sourcePaths = new HashSet<>();
        sourcePaths.add("src/test/resources");
    }

    Set<String> getLibraryPaths() {
        return libraryPaths;
    }

    Set<String> getSourcePaths() {
        return sourcePaths;
    }

}
