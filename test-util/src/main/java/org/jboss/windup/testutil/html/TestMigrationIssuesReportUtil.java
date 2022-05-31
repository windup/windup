package org.jboss.windup.testutil.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TestMigrationIssuesReportUtil extends TestChromeDriverReportUtil {
    private static final String ISSUES_TABLE_CLASS_NAME = "migration-issues-table";

    public boolean checkIssue(String issueName, int numberFound, int effortPerIncident, String levelOfEffort, int totalEffort) {
        List<WebElement> elements = getDriver().findElements(By.className(ISSUES_TABLE_CLASS_NAME));
        if (elements == null || elements.isEmpty()) {
            throw new CheckFailedException("Unable to find " + ISSUES_TABLE_CLASS_NAME + " table element");
        }
        for (WebElement element : elements) {
            if (super.checkValueInTable(element, issueName, String.valueOf(numberFound), String.valueOf(effortPerIncident),
                    levelOfEffort, String.valueOf(totalEffort)))
                return true;
        }

        return false;
    }
}
