package org.jboss.windup.testutil.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Used to assist the unit tests in validating the contents of the EJB Report.
 * 
 * @author jsightler
 *
 */
public class TestEJBReportUtil extends TestReportUtil
{
    public enum EJBType
    {
        MDB,
        STATELESS,
        STATEFUL
    }

    /**
     * Checks that an EJB of the given type and classname is listed
     */
    public boolean checkBeanInReport(EJBType ejbType, String beanName, String className)
    {
        String tableID;
        switch (ejbType)
        {
        case MDB:
            tableID = "mdbTable";
            break;
        case STATELESS:
            tableID = "statelessTable";
            break;
        case STATEFUL:
            tableID = "statefulTable";
            break;
        default:
            throw new IllegalArgumentException("Unexpected type: " + ejbType);
        }

        WebElement element = getDriver().findElement(By.id(tableID));
        if (element == null)
        {
            throw new CheckFailedException("Unable to find ejb beans table element");
        }
        return checkValueInTable(element, beanName, className);
    }
}
