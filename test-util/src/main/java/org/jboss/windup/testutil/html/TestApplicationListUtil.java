package org.jboss.windup.testutil.html;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.util.exception.WindupException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 * Contains methods for evaluating and retrieving data from the application list report.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TestApplicationListUtil extends TestChromeDriverReportUtil {
    /**
     * Gets the total story points for the given application name. This will return -1 if it
     * could not find the story point information.
     */
    public int getTotalStoryPoints(String applicationName) {
        return getEffortPoints(applicationName, "total");
    }

    /**
     * Gets the shared story points for the given application name. This will return -1 if it
     * could not find the story point information.
     */
    public int getSharedStoryPoints(String applicationName) {
        return getEffortPoints(applicationName, "shared");
    }

    /**
     * Gets the unique story points for the given application name. This will return -1 if it
     * could not find the story point information.
     */
    public int getUniqueStoryPoints(String applicationName) {
        return getEffortPoints(applicationName, "unique");
    }

    public void sortApplicationListByEffortPoints() {
        String xpathSortDiv = "//div[@id = 'sort']";
        WebElement sortDiv = getDriver().findElement(By.xpath(xpathSortDiv));
        sortDiv.findElement(By.xpath("./div/button")).click();

        WebElement storyPointsSort = sortDiv.findElement(By.xpath("./div/ul/li/a[contains(text(), 'Story Points')]"));
        storyPointsSort.click();
    }

    public void reverseSortOrder() {
        String xpathSortDiv = "//button[@id = 'sort-order']";
        getDriver().findElement(By.xpath(xpathSortDiv)).click();
    }

    /**
     * Returns the list of application names on the application list.
     */
    public List<String> getApplicationNames() {
        List<String> result = new ArrayList<>();

        List<WebElement> appInfoElements = getDriver().findElements(By.cssSelector(".appInfo"));
        for (WebElement appInfoRow : appInfoElements) {
            WebElement filename = appInfoRow.findElement(By.cssSelector(".fileName"));
            if (filename != null && filename.getText() != null)
                result.add(filename.getText().trim());
        }

        return Collections.unmodifiableList(result);
    }

    public WebElement getApplicationTargetRuntimeLegendHeader() {
        return getDriver().findElement(By.id("runtimeLegendHeader"));
    }

    public WebElement getApplicationTargetRuntimeLegendContent() {
        return getDriver().findElement(By.id("runtimeLegendContent"));
    }

    public List<WebElement> getApplicationTargetRuntimeLabels(String applicationName) {
        List<WebElement> result = new ArrayList<>();

        List<WebElement> appInfoElements = getDriver().findElements(By.cssSelector(".appInfo"));
        for (WebElement appInfoRow : appInfoElements) {
            WebElement filename = appInfoRow.findElement(By.cssSelector(".fileName"));
            if (filename != null) {
                WebElement tagLink = filename.findElement(By.tagName("a"));
                if (!tagLink.getText().trim().equals(applicationName)) {
                    continue;
                }

                WebElement tagDiv = filename.findElement(By.tagName("div"));
                result.addAll(tagDiv.findElements(By.tagName("a")));
            }
        }

        return Collections.unmodifiableList(result);
    }

    public List<WebElement> getApplicationTechLabels(String applicationName) {
        List<WebElement> result = new ArrayList<>();

        List<WebElement> appInfoElements = getDriver().findElements(By.cssSelector(".appInfo"));
        for (WebElement appInfoRow : appInfoElements) {
            WebElement filename = appInfoRow.findElement(By.cssSelector(".fileName"));
            if (filename != null) {
                WebElement tagLink = filename.findElement(By.tagName("a"));
                if (!tagLink.getText().trim().equals(applicationName)) {
                    continue;
                }

                List<WebElement> techLabels = appInfoRow.findElements(By.cssSelector(".techs .label"));
                result.addAll(techLabels);
            }
        }

        return Collections.unmodifiableList(result);
    }

    public void clickTag(String applicationName, String tagName) {
        WebElement applicationRow = getApplicationRow(applicationName);
        if (applicationRow == null)
            throw new WindupException("Could not find application: " + applicationName);

        String xpath = "./div[contains(@class, 'traits')]/div[contains(@class, 'techs')]//span[contains(text(), '" + tagName + "')]/..";
        WebElement tagElement = applicationRow.findElement(By.xpath(xpath));

        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click()", tagElement);
    }

    public boolean isDisplayed(String applicationName) {
        WebElement applicationRow = getApplicationRow(applicationName);
        if (applicationRow == null)
            return false;

        return applicationRow.isDisplayed();
    }

    /**
     * Type should be 'shared' for shared and 'unique' for total.
     */
    private int getEffortPoints(String applicationName, String type) {
        WebElement applicationRow = getApplicationRow(applicationName);
        if (applicationRow == null)
            return -1;

        String xpath = "./div[contains(@class, 'stats')]/div[contains(@class, 'effortPoints') and contains(@class, '" + type + "')]";
        WebElement pointsSectionElement = applicationRow.findElement(By.xpath(xpath));

        WebElement effortPointsElement = pointsSectionElement.findElement(By.cssSelector(".points"));

        return Integer.parseInt(effortPointsElement.getText());
    }

    private WebElement getApplicationRow(String applicationName) {
        List<WebElement> appInfoElements = getDriver().findElements(By.cssSelector(".appInfo"));
        for (WebElement appInfoRow : appInfoElements) {
            WebElement filename = appInfoRow.findElement(By.cssSelector(".fileName"));
            if (filename != null && StringUtils.equals(filename.getText(), applicationName))
                return appInfoRow;
        }
        return null;
    }
}
