package org.jboss.windup.tests.bootstrap;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class UnknownCommandLineArgumentTest extends AbstractBootstrapTest {
    @Test
    public void test() {
        bootstrap("--foobar");
        assertTrue(capturedOutput().contains("WARNING: Unrecognized command-line argument: --foobar"));
    }
}
