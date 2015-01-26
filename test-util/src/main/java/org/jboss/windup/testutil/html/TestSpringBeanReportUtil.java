package org.jboss.windup.testutil.html;

import java.util.List;

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
     * Checks that a Spring Bean is listed with the given name and classname
     */
    public boolean checkSpringBeanInReport(String beanName, String className)
    {
        WebElement element = getDriver().findElement(By.id("springBeansTable"));
        if (element == null)
        {
            throw new CheckFailedException("Unable to spring beans table element");
        }
        List<WebElement> rowElements = element.findElements(By.xpath(".//tr"));
        return checkValueInTable(element, beanName, className);
    }
}
