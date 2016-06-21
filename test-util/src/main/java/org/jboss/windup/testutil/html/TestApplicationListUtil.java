package org.jboss.windup.testutil.html;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Contains methods for evaluating and retrieving data from the application list report.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TestApplicationListUtil extends TestReportUtil
{
    /**
     * Gets the total story points for the given application name. This will return -1 if it
     * could not find the story point information.
     */
    public int getTotalStoryPoints(String applicationName)
    {
        return getEffortPoints(applicationName, "total");
    }

    /**
     * Gets the shared story points for the given application name. This will return -1 if it
     * could not find the story point information.
     */
    public int getSharedStoryPoints(String applicationName)
    {
        return getEffortPoints(applicationName, "shared");
    }

    /**
     * Gets the unique story points for the given application name. This will return -1 if it
     * could not find the story point information.
     */
    public int getUniqueStoryPoints(String applicationName)
    {
        return getEffortPoints(applicationName, "unique");
    }

    /**
     * Type should be 'shared' for shared and 'unique' for total.
     */
    private int getEffortPoints(String applicationName, String type)
    {
        WebElement applicationRow = getApplicationRow(applicationName);
        if (applicationRow == null)
            return -1;

        String xpath = "./div[contains(@class, 'stats')]/div[contains(@class, 'effortPoints') and contains(@class, '" + type + "')]";
        WebElement pointsSectionElement = applicationRow.findElement(By.xpath(xpath));

        WebElement effortPointsElement = pointsSectionElement.findElement(By.cssSelector(".points"));

        return Integer.parseInt(effortPointsElement.getText());
    }

    private WebElement getApplicationRow(String applicationName)
    {
        List<WebElement> appInfoElements = getDriver().findElements(By.cssSelector(".appInfo"));
        for (WebElement appInfoRow : appInfoElements)
        {
            WebElement filename = appInfoRow.findElement(By.cssSelector(".fileName"));
            if (filename != null && StringUtils.equals(filename.getText(), applicationName))
                return appInfoRow;
        }
        return null;
    }
}
