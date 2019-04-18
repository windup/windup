package org.jboss.windup.testutil.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TestJPAReportUtil extends TestChromeDriverReportUtil {

    /**
     * Checks that an EJB of the given type and classname is listed with given columns in the table
     */
    public boolean checkEntityInReport(String... columns)
    {
        String tableID = "jpaEntityTable";

        WebElement element = getDriver().findElement(By.id(tableID));
        if (element == null)
        {
            throw new CheckFailedException("Unable to find ejb beans table element");
        }
        return checkValueInTable(element, columns);
    }

}
