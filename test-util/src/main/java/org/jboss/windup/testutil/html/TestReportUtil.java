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

    public void loadPage(Path filePath)
    {
        getDriver().get(filePath.toUri().toString());
    }

    public WebDriver getDriver()
    {
        return driver;
    }
}
