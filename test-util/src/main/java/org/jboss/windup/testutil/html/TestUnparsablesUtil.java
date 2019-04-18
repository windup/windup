package org.jboss.windup.testutil.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Contains methods for testing the Java Application Overview report.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class TestUnparsablesUtil extends TestChromeDriverReportUtil
{
    /**
     * Checks that a Hibernate entity is listed with the given entity classname and tablename
     */
    public boolean checkUnparsableFileInReport(String sectionName, String fileName)
    {
        List<WebElement> tables = getDriver().findElements(By.className("unparsableFiles"));
        if (tables == null || tables.isEmpty())
        {
            throw new CheckFailedException("Can't find the table unparsableFiles");
        }

        for (WebElement table : tables)
        {
            WebElement headingElement = table.findElement(By.xpath("..")).findElement(By.className("panel-title"));
            if (headingElement.getText().trim().equals(sectionName))
            {
                return checkStringInRows(table, fileName);
            }
        }
        return false;
    }


    /**
     * Checks that the table contains a row with the given first two columns
     */
    private boolean checkStringInRows(WebElement table, String fileName)
    {
        fileName = fileName.replace("'", "\'");
        String xpath = ".//tr/td[ contains(., '" + fileName + "') ]"; // 0
        List<WebElement> rowElements = table.findElements(By.xpath(xpath));
        return rowElements.size() != 0;
    }
}
