/**
 *
 */
package org.jboss.windup.testutil.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains methods for testing the Jar Dependency report.
 *
 * @author mnovotny
 *
 */
public class TestDependencyReportUtil extends TestChromeDriverReportUtil {

    public boolean findDependencyElement(String fileName, String gav, String dependencyHash, String version, String org, List<String> paths) {
        return findDependencyElement(fileName, null, gav, dependencyHash, version, org, paths);
    }

    public boolean findDependencyElement(String fileName, String name, String gav, String dependencyHash, String version, String org, List<String> paths) {
        List<WebElement> elements = getDriver().findElements(By.xpath("//*[@id='" + fileName + "']"));
        if (elements == null || elements.isEmpty()) {
            throw new CheckFailedException("Unable to find " + "dependencies");
        }

        if (elements.size() == 1) {
            WebElement dependency = elements.get(0);
            WebElement h3 = dependency.findElement(By.tagName("h3"));
            if (h3 != null && h3.getText().equals(fileName)) {
                WebElement dependencyParent = dependency.findElement(By.xpath(".."));
                return checkDependency(dependencyParent, name, fileName, gav, dependencyHash, version, org, paths);
            }
        } else {
            throw new CheckFailedException("Found more than one dependency of the same filename - " + fileName);
        }
        return false;
    }

    public int getNumberOfJarsOnPage() {
        List<WebElement> elements = getDriver().findElements(By.className("dependency"));
        return (elements != null) ? elements.size() : 0;
    }

    public int getNumberOfArchivePathsOnPage(String archiveName) {
        String id = archiveName + "-paths";
        //List<WebElement> pathElements = getDriver().findElements(By.id(id));
        List<WebElement> pathElements = getDriver().findElements(By.xpath("//ul[@id='" + id + "']/li"));
        return (pathElements != null) ? pathElements.size() : 0;
    }

    boolean checkDependency(WebElement dependencyElement, String name, String fileName, String gav, String dependencyHash, String version, String org, List<String> paths) {
        boolean found = false;
        //1. Maven coordinates 
        if (gav != null && !gav.isEmpty()) {
            found = isDependencyPropertyExists(dependencyElement, "maven", gav, fileName);
            if (found) {
                found = isDependencyPropertyURLExists(dependencyElement, "maven", gav, fileName);
            }
        }
        //2. SHA1 hash
        if (dependencyHash != null && !dependencyHash.isEmpty() && found) {
            found = isDependencyPropertyExists(dependencyElement, "hash", dependencyHash, fileName);
        }
        //3. Name
        if (name != null && !name.isEmpty() && found) {
            found = isDependencyPropertyExists(dependencyElement, "name", name, fileName);
        }
        //4. Version
        if (version != null && !version.isEmpty() && found) {
            found = isDependencyPropertyExists(dependencyElement, "version", version, fileName);
        }
        //5. Organization
        if (org != null && !org.isEmpty() && found) {
            found = isDependencyPropertyExists(dependencyElement, "org", org, fileName);
        }
        //6. Found paths
        if (paths != null && !paths.isEmpty() && found) {
            found = isDependencyPathsExists(dependencyElement, "paths", paths, fileName);
        }
        return found;
    }

    private boolean isDependencyPathsExists(WebElement traitsElement, String key, final List<String> foundPaths, String dependencyName) {
        String id = dependencyName + "-" + key;
        try {
            List<WebElement> pathElements = traitsElement.findElements(By.xpath("//ul[@id='" + id + "']/li"));
            if (pathElements.size() != foundPaths.size()) {
                return false;
            }
            List<String> pathsOnPage = new ArrayList<>();
            for (WebElement webElement : pathElements) {
                pathsOnPage.add(webElement.getText());
            }

            pathsOnPage.removeAll(foundPaths);
            if (pathsOnPage.isEmpty()) {
                return true;
            }
        } catch (org.openqa.selenium.NoSuchElementException e) {
            System.err.println("Element not found " + e.getLocalizedMessage());
        }
        return false;
    }

    private boolean isDependencyPropertyURLExists(WebElement traitsElement, String key, String value, String dependencyName) {
        String id = dependencyName + "-" + key;
        try {
            WebElement header = traitsElement.findElement(By.id(id));
            if (header != null) {
                WebElement url = traitsElement.findElement(By.partialLinkText(value));
                if (url != null) {
                    return true;
                }
            }
        } catch (org.openqa.selenium.NoSuchElementException e) {
            System.err.println("Element not found " + e.getLocalizedMessage());
        }
        return false;
    }

    private boolean isDependencyPropertyExists(WebElement traitsElement, String key, String value, String dependencyName) {
        String id = dependencyName + "-" + key;
        try {
            WebElement header = traitsElement.findElement(By.id(id));
            if (header != null && header.getText().endsWith(value)) {
                return true;
            }
        } catch (org.openqa.selenium.NoSuchElementException e) {
            System.err.println("Element not found " + e.getLocalizedMessage());
        }
        return false;
    }

}
