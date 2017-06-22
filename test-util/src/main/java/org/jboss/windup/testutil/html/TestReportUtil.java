package org.jboss.windup.testutil.html;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.util.exception.WindupException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Contains utility methods for assisting tests in interacting with the generated reports.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TestReportUtil
{
    private final WebDriver driver;

    public TestReportUtil()
    {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setBrowserName("firefox");
        capabilities.setJavascriptEnabled(true);
        this.driver = new HtmlUnitDriver(capabilities);
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
     * The purpose of this method is to show the expected and actual row numbers when a test fails.
     */
    void assertValueInTableRowByFirstColumn(WebElement element, String firstColumnVal, String... expectedValues)
    {
        List<WebElement> rowElements = element.findElements(By.xpath(".//tr"));
        for (WebElement rowElement : rowElements)
        {
            // Check if the first column matches the key
            List<WebElement> firstTd = rowElement.findElements(By.xpath("./td[position() = 1]"));
            if (firstTd.size() != 1)
                continue;
            String actualKey = firstTd.get(0).getText().trim();
            if (!actualKey.equals(firstColumnVal))
                continue;

            boolean rowMatches = true;

            List<String> actualValues = new ArrayList(1 + expectedValues.length);
            for (int i = 0; i < expectedValues.length; i++)
            {
                String expectedValue = expectedValues[i];
                List<WebElement> tdElements = rowElement.findElements(By.xpath("./td[position() = " + (i + 1 + 1) + "]"));
                if (tdElements.size() != 1)
                    break;
                String actualValue = tdElements.get(0).getText().trim();
                actualValues.add(actualValue);
                if (!actualValue.trim().equals(expectedValue.trim()))
                    rowMatches = false;

            }
            if (rowMatches)
                return;
            else
                throw new WindupException(String.format(
                        "The row starting with '%s' did not match."
                        + "\n    Expected: %s"
                        + "\n    Actual: %s",
                        firstColumnVal,
                        StringUtils.join(expectedValues, ", "),
                        StringUtils.join(actualValues, ", ")
                ));
        }

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
                List<WebElement> tdElements = rowElement.findElements(By.xpath("./td[position() = " + (i + 1) + "]"));
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
