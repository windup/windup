package org.jboss.windup.testutil.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Used for assisting the unit tests in validating the contents of the Spring Bean page.
 * 
 * @author jsightler
 *
 */
public class TestSpringBeanReportUtil extends TestReportUtil
{
    /**
     * Checks that for the given filename, location, and IP
     */
    public boolean checkSpringBeanInReport(String beanName, String className)
    {
        WebElement element = getDriver().findElement(By.id("springBeansTable"));
        if (element == null)
        {
            throw new CheckFailedException("Unable to spring beans table element");
        }
        return checkValueInTable(element, beanName, className);
    }
}
