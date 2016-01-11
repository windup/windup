package org.jboss.windup.tests.bootstrap;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class ListAddonsCommandTest extends AbstractBootstrapTest {
    @Test
    public void test() throws IOException {
        bootstrap("--list");

        assertTrue(capturedOutput().contains(ADDON_REPOSITORY));
        assertTrue(capturedOutput().contains("Enabled addons:"));
        assertTrue(capturedOutput().contains("org.jboss.forge.addon"));
        assertTrue(capturedOutput().contains("org.jboss.windup"));
    }
}
