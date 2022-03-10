package org.jboss.windup.tests.bootstrap;

import static org.junit.Assert.assertTrue;

import org.jboss.windup.util.ThemeProvider;
import org.junit.Before;
import org.junit.Test;

public class DisplayHelpCommandTest extends AbstractBootstrapTest
{
    @Before
    public void insureHelpCacheIsAvailable()
    {
        bootstrap("--generateCaches");
    }

    @Test
    public void noArgument()
    {
        test();
    }

    @Test
    public void longArgument()
    {
        test("--help");
    }

    @Test
    public void shortArgument()
    {
        test("-h");
    }

    private void test(String... args)
    {
        bootstrap(args);

        assertTrue(capturedOutput().contains(ThemeProvider.getInstance().getTheme().getBrandNameAcronym() + " CLI Options:"));
        assertTrue(capturedOutput().contains("--target"));
        assertTrue(capturedOutput().contains("Forge Options:"));
        assertTrue(capturedOutput().contains("--version"));
    }
}
