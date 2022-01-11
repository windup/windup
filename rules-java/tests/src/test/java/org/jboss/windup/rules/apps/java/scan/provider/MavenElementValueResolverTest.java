package org.jboss.windup.rules.apps.java.scan.provider;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.windup.rules.apps.java.scan.provider.DiscoverMavenProjectsRuleProvider.*;

public class MavenElementValueResolverTest {
    
    private final MavenElementValueResolver mavenElementValueResolver = new MavenElementValueResolver();

    private static final Map<String, String> namespaces = new HashMap<>();
    static {
        namespaces.put("pom", "http://maven.apache.org/POM/4.0.0");
    }
    
    @Test
    public void shouldResolveFieldWithMultipleProperties() throws Exception {
        String inputDir = "src/test/resources/org/jboss/windup/rules/java";
        File file = new File(inputDir + "/pom-property-with-multiple-variables.xml");
        
        FileInputStream pomFIS = new FileInputStream(file);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document pom = builder.parse(pomFIS);
        
        String elementValue = mavenElementValueResolver.resolveValue(pom, namespaces, "${version.major}.${version.minor}","0.0.1-SNAPSHOT");

        Assert.assertEquals("1.2", elementValue);
    }

    @Test
    public void shouldResolveFieldWithSingleProperty() throws Exception {
        String inputDir = "src/test/resources/org/jboss/windup/rules/java";
        File file = new File(inputDir + "/pom-property-with-multiple-variables.xml");

        FileInputStream pomFIS = new FileInputStream(file);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document pom = builder.parse(pomFIS);

        String elementValue = mavenElementValueResolver.resolveValue(pom, namespaces, "${version.major}","0.0.1-SNAPSHOT");

        Assert.assertEquals("1", elementValue);
    }
}
