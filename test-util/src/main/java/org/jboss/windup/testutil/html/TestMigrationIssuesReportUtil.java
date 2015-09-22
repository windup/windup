package org.jboss.windup.testutil.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TestMigrationIssuesReportUtil extends TestReportUtil
{
    private static final String TABLE_ID = "issues_table";

    public boolean checkIssue(String issueName, int numberFound, int effortPerIncident, int totalEffort)
    {
        WebElement element = getDriver().findElement(By.id(TABLE_ID));
        if (element == null)
        {
            throw new CheckFailedException("Unable to find ejb beans table element");
        }
        return super.checkValueInTable(element, issueName, String.valueOf(numberFound), String.valueOf(effortPerIncident),
                    String.valueOf(totalEffort));
    }
}
