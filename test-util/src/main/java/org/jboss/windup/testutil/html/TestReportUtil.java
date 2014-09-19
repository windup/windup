package org.jboss.windup.testutil.html;

import java.nio.file.Path;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Contains utility methods for assisting tests in interacting with the generated reports.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class TestReportUtil
{
    private WebDriver driver;

    public TestReportUtil()
    {
        this.driver = new HtmlUnitDriver(false);
    }

    /**
     * Loads the given page w/ the {@link WebDriver}
     */
    public void loadPage(Path filePath)
    {
        getDriver().get(filePath.toUri().toString());
    }

    protected WebDriver getDriver()
    {
        return driver;
    }
}
