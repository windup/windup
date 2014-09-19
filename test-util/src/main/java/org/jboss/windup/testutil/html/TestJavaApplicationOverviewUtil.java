package org.jboss.windup.testutil.html;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Contains methods for testing the Java Application Overview report.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class TestJavaApplicationOverviewUtil extends TestReportUtil
{

    /**
     * Checks if the given App section, filepath, and tag can be found in the report.
     * 
     * For example calling checkFilePathAndIssues("src_example", "src/main/resources/test.properties",
     * "Web Servlet again") will ensure that an application called "src_example" is in the report, with a line
     * referencing "src/main/resources/test.properties" and that this line contains text in the issues section saying
     * "Web Servlet again").
     */
    public boolean checkFilePathAndIssues(String appSection, String filePath, String text)
    {
        WebElement appSectionEl = getAppSectionElement(appSection);
        if (appSectionEl == null)
        {
            return false;
        }

        WebElement fileRowElement = getFileRowElement(appSection, filePath);
        if (fileRowElement == null)
        {
            return false;
        }

        List<WebElement> elements = fileRowElement.findElements(By.xpath("./td[position() = 3]"));
        for (WebElement element : elements)
        {
            if (element.getText() != null && element.getText().contains(text))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the given App section, filepath, and tag can be found in the report.
     * 
     * For example calling checkFilePathAndTag("src_example", "src/main/resources/test.properties", "Properties") will
     * ensure that an application called "src_example" is in the report, with a line referencing
     * "src/main/resources/test.properties" and that this file is tagged "Properties"
     */
    public boolean checkFilePathAndTag(String appSection, String filePath, String tag)
    {
        WebElement appSectionEl = getAppSectionElement(appSection);
        if (appSectionEl == null)
        {
            return false;
        }

        WebElement fileRowElement = getFileRowElement(appSection, filePath);
        if (fileRowElement == null)
        {
            return false;
        }

        List<WebElement> elements = fileRowElement.findElements(By.xpath("./td[position() = 2]/span"));
        for (WebElement element : elements)
        {
            if (element.getText() != null && element.getText().equals(tag))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the given App section, filepath, and tag can be found in the report.
     * 
     * For example calling checkFilePathAndTag("src_example", "src/main/resources/test.properties") will ensure that an
     * application called "src_example" is in the report, with a line referencing "src/main/resources/test.properties"
     */
    public boolean checkFilePath(String appSection, String filePath)
    {
        WebDriver driver = getDriver();

        WebElement appSectionEl = getAppSectionElement(appSection);
        if (appSectionEl == null)
        {
            return false;
        }

        WebElement fileRowElement = getFileRowElement(appSection, filePath);
        if (fileRowElement == null)
        {
            return false;
        }

        return true;
    }

    private WebElement getFileRowElement(String appSection, String filePath)
    {
        WebElement fileTable = getAppSectionElement(appSection).findElement(By.xpath("../../table"));

        WebElement fileRow = fileTable.findElement(By
                    .xpath("./tbody/tr/td/a[normalize-space(text()) = '" + filePath + "']/../.."));
        return fileRow;
    }

    private WebElement getAppSectionElement(String appSection)
    {
        List<WebElement> titleElements = getDriver().findElements(By.className("panel-title"));
        for (WebElement el : titleElements)
        {
            if (el.getText() != null && appSection.equals(el.getText().trim()))
            {
                return el;
            }
        }
        return null;
    }
}
