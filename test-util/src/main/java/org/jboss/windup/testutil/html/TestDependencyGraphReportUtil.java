/**
 *
 */
package org.jboss.windup.testutil.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Contains methods for testing the Dependency Graph report.
 *
 * @author mrizzi
 *
 */
public class TestDependencyGraphReportUtil extends TestReportUtil
{
    public TestDependencyGraphReportUtil() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        this.driver = new ChromeDriver(chromeOptions);
    }

    @Override
    public void loadPage(Path filePath)
    {
        try {
            if (!filePath.toFile().exists())
                throw new CheckFailedException("Requested page file does not exist: " + filePath);
            driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
            driver.get(filePath.toUri().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public int getNumberOfArchivesInTheGraph()
    {
        List<WebElement> dependencies = getDriver().findElements(By.tagName("g"));
        return (dependencies != null) ?  dependencies.size() : 0;
    }

    public long getNumberOfArchivesInTheGraphByName(String withName)
    {
        return  getDriver().findElements(By.tagName("title")).stream().filter(webElement -> withName.equals(webElement.getText())).count();
    }

    public int getNumberOfRelationsInTheGraph()
    {
        List<WebElement> relations = getDriver().findElements(By.tagName("line"));
        return (relations != null) ?  relations.size() : 0;
    }
}
