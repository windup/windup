package org.jboss.windup.testutil.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Tests the contents of the static ip report
 */
public class TestStaticIPReportUtil extends TestReportUtil
{

    /**
     * Checks that a sis listed with the given filename, location, and IP
     */
    public boolean checkStaticIPInReport(String filename, String locationInFile, String ip)
    {
        WebElement element = getDriver().findElement(By.id("staticIPTable"));
        if (element == null)
        {
            throw new CheckFailedException("Unable to find static IP table element");
        }
        return checkValueInTable(element, filename, locationInFile, ip);
    }
}
