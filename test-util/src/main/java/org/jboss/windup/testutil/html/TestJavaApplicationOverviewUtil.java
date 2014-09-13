package org.jboss.windup.testutil.html;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TestJavaApplicationOverviewUtil extends TestReportUtil
{

    public boolean checkFilePathAndTag(String appSection, String filePath, String tag)
    {
        WebElement appSectionEl = getAppSectionElement(appSection);
        if (appSectionEl == null)
        {
            return false;
        }

        WebElement fileRowElement = getFileRowElement(appSection, filePath);
        if (fileRowElement == null)
        {
            return false;
        }

        List<WebElement> elements = fileRowElement.findElements(By.xpath("./td[position() = 2]/span"));
        for (WebElement element : elements)
        {
            if (element.getText() != null && element.getText().equals(tag))
            {
                return true;
            }
        }

        return false;
    }

    public boolean checkFilePath(String appSection, String filePath)
    {
        WebDriver driver = getDriver();

        WebElement appSectionEl = getAppSectionElement(appSection);
        if (appSectionEl == null)
        {
            return false;
        }

        WebElement fileRowElement = getFileRowElement(appSection, filePath);
        if (fileRowElement == null)
        {
            return false;
        }

        return true;
    }

    private WebElement getFileRowElement(String appSection, String filePath)
    {
        WebElement fileTable = getAppSectionElement(appSection).findElement(By.xpath("../../table"));

        WebElement fileRow = fileTable.findElement(By
                    .xpath("./tbody/tr/td/a[normalize-space(text()) = '" + filePath + "']/../.."));
        return fileRow;
    }

    private WebElement getAppSectionElement(String appSection)
    {
        List<WebElement> titleElements = getDriver().findElements(By.className("panel-title"));
        for (WebElement el : titleElements)
        {
            if (el.getText() != null && appSection.equals(el.getText().trim()))
            {
                return el;
            }
        }
        return null;
    }
}
