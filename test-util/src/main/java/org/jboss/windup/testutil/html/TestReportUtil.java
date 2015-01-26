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
    boolean checkValueInTable(WebElement element, String column1Expected, String column2Expected)
    {
        List<WebElement> rowElements = element.findElements(By.xpath(".//tr"));
        boolean foundExpectedResult = false;
        for (WebElement rowElement : rowElements)
        {
            List<WebElement> td1Elements = rowElement.findElements(By.xpath(".//td[position() = 1]"));
            List<WebElement> td2Elements = rowElement.findElements(By.xpath(".//td[position() = 2]"));
            if (td1Elements.size() != 1 || td2Elements.size() != 1)
            {
                continue;
            }

            String column1 = td1Elements.get(0).getText().trim();
            String column2 = td2Elements.get(0).getText().trim();
            if (column1.equals(column1Expected) && column2.equals(column2Expected))
            {
                foundExpectedResult = true;
                break;
            }
        }
        return foundExpectedResult;
    }
}
