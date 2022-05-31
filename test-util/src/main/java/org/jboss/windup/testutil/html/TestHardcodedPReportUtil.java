package org.jboss.windup.testutil.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Tests the contents of the hardcoded ip report
 */
public class TestHardcodedPReportUtil extends TestChromeDriverReportUtil {

    /**
     * Checks that a sis listed with the given filename, location, and IP
     */
    public boolean checkHardcodedIPInReport(String filename, String locationInFile, String ip) {
        WebElement element = getDriver().findElement(By.id("hardcodedIPTable"));
        if (element == null) {
            throw new CheckFailedException("Unable to find hard-coded IP table element");
        }
        return checkValueInTable(element, filename, locationInFile, ip);
    }
}
