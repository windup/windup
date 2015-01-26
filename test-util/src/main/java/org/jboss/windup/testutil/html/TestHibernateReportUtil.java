package org.jboss.windup.testutil.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Contains methods for testing the contents of the Hibernate report.
 *
 */
public class TestHibernateReportUtil extends TestReportUtil
{
    /**
     * Checks that a Hibernate property is listed with the given key and value
     */
    public boolean checkSessionFactoryPropertyInReport(String propKey, String propValue)
    {
        WebElement element = getDriver().findElement(By.id("sessionFactoryPropertiesTable"));
        if (element == null)
        {
            throw new CheckFailedException("Unable to find hibernate session factory table element");
        }
        return checkValueInTable(element, propKey, propValue);
    }

    /**
     * Checks that a Hibernate entity is listed with the given entity classname and tablename
     */
    public boolean checkHibernateEntityInReport(String classname, String tablename)
    {
        WebElement element = getDriver().findElement(By.id("hibernateEntityTable"));
        if (element == null)
        {
            throw new CheckFailedException("Unable to find hibernate entity table element");
        }
        return checkValueInTable(element, classname, tablename);
    }
}
