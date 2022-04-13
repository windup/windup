package org.jboss.windup.rules.java.ip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.InlineHintService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class DiscoverHardcodedIPAddressTest
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testStaticIPScanner() throws IOException, InstantiationException, IllegalAccessException
    {
        try (GraphContext context = factory.create(true))
        {
            Path inputPath = Paths.get("src/test/resources/staticip");

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                        "windup_" + RandomStringUtils.randomAlphanumeric(6));
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            Predicate<RuleProvider> predicate = new NotPredicate(new RuleProviderPhasePredicate(ReportGenerationPhase.class));

            WindupConfiguration windupConfiguration = new WindupConfiguration()
                        .setRuleProviderFilter(predicate)
                        .setGraphContext(context);
            windupConfiguration.addInputPath(inputPath);
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);

            Set<String> expectedIPs = new HashSet<>();
            expectedIPs.add("192.168.0.1");
            expectedIPs.add("192.168.0.2");
            expectedIPs.add("192.168.0.7");
            expectedIPs.add("192.168.0.13");
            expectedIPs.add("10.10.1.0");
            expectedIPs.add("10.10.1.1");
            expectedIPs.add("10.10.1.2");
            expectedIPs.add("10.10.1.3");

            Set<String> unexpectedIPs = new HashSet<>();
            unexpectedIPs.add("192.168.0.3");
            unexpectedIPs.add("192.168.0.4");
            unexpectedIPs.add("192.168.0.5");
            unexpectedIPs.add("192.168.0.6");
            unexpectedIPs.add("192.168.270.8");
            unexpectedIPs.add("192.168.0.9.3.4");
            unexpectedIPs.add("192.168.0.12");
            unexpectedIPs.add("10.10.1.4");

            InlineHintService service = new InlineHintService(context);
            Pattern ipExtractor = Pattern.compile("\\*\\*Hard-coded IP: (.*?)\\*\\*");
            int numberFound = 0;
            for (InlineHintModel hint : service.findAll())
            {
                if (StringUtils.equals("Hard-coded IP address", hint.getTitle()))
                {
                    Matcher matcher = ipExtractor.matcher(hint.getHint());
                    if (matcher.find())
                    {
                        String ip = matcher.group(1);
                        if (unexpectedIPs.contains(ip))
                            Assert.fail("This IP (" + ip + ") should not have been marked valid");
                        else if (!expectedIPs.contains(ip))
                            Assert.fail("This IP (" + ip + ") was detected, but was not in the expected list");
                        numberFound++;
                    }
                    else
                    {
                        Assert.fail("Hint format not recognized: " + hint.getHint());
                    }
                }
            }
            Assert.assertEquals(expectedIPs.size(), numberFound);
        }
    }

}
