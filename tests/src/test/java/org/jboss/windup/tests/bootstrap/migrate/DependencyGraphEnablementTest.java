package org.jboss.windup.tests.bootstrap.migrate;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.jboss.windup.bootstrap.Bootstrap;
import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DependencyGraphEnablementTest extends AbstractBootstrapTestWithRules
{
   @Rule
   public final TemporaryFolder tmp = new TemporaryFolder();

   @Rule
   public final TemporaryFolder tmpAddonDir = new TemporaryFolder();

   @Test
   public void dependencyGraphEnabledByDefault()
   {
      bootstrap("--addonDir", tmpAddonDir.getRoot().getAbsolutePath(),
               "--install", "org.jboss.windup.rules.apps:windup-rules-tattletale," + Bootstrap.getVersion());

      bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
               "--output", tmp.getRoot().getAbsolutePath(),
               "--source", "eap6",
               "--target", "eap7",
               "--addonDir", tmpAddonDir.getRoot().getAbsolutePath());

      assertTrue(Files.exists(Paths.get(tmp.getRoot().getAbsolutePath(), "reports", "dependency_graph_report.html")));
   }

   @Test
   public void dependencyGraphEnabledWithSourceMode()
   {
      bootstrap("--input", "../test-files/src_example",
               "--output", tmp.getRoot().getAbsolutePath(),
               "--source", "eap6",
               "--target", "eap7",
               "--sourceMode");

      assertTrue(Files.exists(Paths.get(tmp.getRoot().getAbsolutePath(), "reports", "dependency_graph_report.html")));
   }

}
