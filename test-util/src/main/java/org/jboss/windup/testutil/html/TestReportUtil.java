package org.jboss.windup.testutil.html;

import java.nio.file.Path;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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

    /**
     * Checks that the table contains a row with the given first two columns
     */
    boolean checkValueInTable(WebElement element, String... columnValues)
    {
        List<WebElement> rowElements = element.findElements(By.xpath(".//tr"));
        boolean foundExpectedResult = false;
        for (WebElement rowElement : rowElements)
        {
            boolean rowMatches = true;
            for (int i = 0; i < columnValues.length; i++)
            {
                String expectedValue = columnValues[i];
                List<WebElement> tdElements = rowElement.findElements(By.xpath(".//td[position() = " + (i + 1) + "]"));
                if (tdElements.size() != 1)
                {
                    rowMatches = false;
                    break;
                }
                String actualValue = tdElements.get(0).getText().trim();
                if (!actualValue.trim().equals(expectedValue.trim()))
                {
                    rowMatches = false;
                    break;
                }
            }
            if (rowMatches)
            {
                foundExpectedResult = true;
                break;
            }
        }
        return foundExpectedResult;
    }
}
