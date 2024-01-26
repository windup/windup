package org.jboss.windup.exec.test;

import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.exec.configuration.options.OutputPathOption;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class OutputPathOptionTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    private Path getBasePath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve(getClass().getSimpleName() + "_" + RandomStringUtils.randomAlphanumeric(6));
    }

    @Test
    public void testInputAndOutputEqual() {
        Path basePath = getBasePath();
        Path appPath = basePath.resolve("ApplicationDir");

        ValidationResult result = OutputPathOption.validateInputAndOutputPath(appPath, appPath);
        Assert.assertEquals(ValidationResult.Level.ERROR, result.getLevel());
    }

    @Test
    public void testInputAsSubOfOutput() throws Exception {
        Path basePath = getBasePath();
        Path appPath = basePath.resolve("myapppath");
        FileUtils.forceMkdir(appPath.toFile());
        try {
            ValidationResult result = OutputPathOption.validateInputAndOutputPath(appPath, basePath);
            Assert.assertEquals(ValidationResult.Level.ERROR, result.getLevel());
        } finally {
            FileUtils.deleteQuietly(basePath.toFile());
        }
    }

    @Test
    public void testOutputAsSubOfInput() throws Exception {
        Path basePath = getBasePath();
        Path subpath = basePath.resolve("subpath");
        FileUtils.forceMkdir(subpath.toFile());
        try {
            ValidationResult result = OutputPathOption.validateInputAndOutputPath(basePath, subpath);
            Assert.assertEquals(ValidationResult.Level.ERROR, result.getLevel());
        } finally {
            FileUtils.deleteQuietly(basePath.toFile());
        }
    }

    @Test
    public void testValidConfiguration() throws Exception {
        Path basePath = getBasePath();
        Path input = basePath.resolve("input");
        Path output = basePath.resolve("output");
        FileUtils.forceMkdir(input.toFile());
        FileUtils.forceMkdir(output.toFile());
        try {
            ValidationResult result = OutputPathOption.validateInputAndOutputPath(input, output);
            Assert.assertEquals(ValidationResult.Level.SUCCESS, result.getLevel());
        } finally {
            FileUtils.deleteQuietly(basePath.toFile());
        }
    }
}
