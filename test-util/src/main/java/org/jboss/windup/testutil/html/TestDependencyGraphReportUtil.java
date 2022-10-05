/**
 *
 */
package org.jboss.windup.testutil.html;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Contains methods for testing the Dependency Graph report.
 *
 * @author mrizzi
 *
 */
public class TestDependencyGraphReportUtil extends TestChromeDriverReportUtil {
    public int getNumberOfArchivesInTheGraph() {
        List<WebElement> dependencies = getDriver().findElements(By.tagName("g"));
        return (dependencies != null) ? dependencies.size() : 0;
    }

    public long getNumberOfArchivesInTheGraphByName(String withName) {
        return getDriver().findElements(By.tagName("title")).stream().filter(webElement -> withName.equals(webElement.getText())).count();
    }

    public int getNumberOfRelationsInTheGraph() {
        List<WebElement> relations = getDriver().findElements(By.tagName("line"));
        return (relations != null) ? relations.size() : 0;
    }
}
