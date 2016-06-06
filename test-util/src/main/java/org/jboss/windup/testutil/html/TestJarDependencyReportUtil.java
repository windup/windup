/**
 *
 */
package org.jboss.windup.testutil.html;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Contains methods for testing the Jar Dependency report.
 *
 * @author mnovotny
 *
 */
public class TestJarDependencyReportUtil extends TestReportUtil
{

    public boolean findDependencyElement(String fileName, String name, String gav, String dependencyHash, String version, String org, List<String> paths)
    {
        List<WebElement> elements = getDriver().findElements(By.xpath("//*[@id='" + fileName +"']"));
        if (elements == null || elements.isEmpty())
        {
            throw new CheckFailedException("Unable to find " + "dependencies" );
        }

        if (elements.size() == 1)
        {
            WebElement dependency = elements.get(0);
            WebElement h4 = dependency.findElement(By.tagName("h4"));
            if ( h4 != null && h4.getText().equals(fileName)) 
            {
                return checkDependency(dependency, name, fileName, gav, dependencyHash, version, org, paths);
            }
        }
        else
        {
            throw new CheckFailedException("Found more than one dependency of the same filename - " + fileName);
        }
        return false;
    }
    
    public int getNumberOfJarsOnPage()
    {
        List<WebElement> elements = getDriver().findElements(By.className("dependency"));
        return (elements != null) ?  elements.size() : 0;
    }

    boolean checkDependency (WebElement dependencyElement, String name, String fileName, String gav, String dependencyHash, String version, String org, List paths)
    {
        boolean found = false;
        //1. Maven coordinates 
        if (gav != null && !gav.isEmpty())
            found = isDependencyPropertyExists(dependencyElement, "maven", gav, fileName);
            found = isDependencyPropertyURLExists(dependencyElement, "maven", gav, fileName);
        //2. SHA1 hash
        if (dependencyHash != null && !dependencyHash.isEmpty())
            found = isDependencyPropertyExists(dependencyElement, "hash", dependencyHash, fileName);
        //3. Name
        if (name != null && !name.isEmpty())
            found = isDependencyPropertyExists(dependencyElement, "name", name, fileName);
        //4. Version
        if (version != null && !version.isEmpty())
            found = isDependencyPropertyExists(dependencyElement, "version", version, fileName);
        //5. Organization
        if (org != null && !org.isEmpty())
            found = isDependencyPropertyExists(dependencyElement, "org", org, fileName);
        //6. Found paths
        if (paths != null && !paths.isEmpty())
            found = isDependencyPathsExists(dependencyElement, "paths", paths, fileName);
        return found;
    }
    
    private boolean isDependencyPathsExists(WebElement traitsElement, String key, List<String> foundPathsLog4j, String dependencyName)
    {
        String id = dependencyName + "-"+key;
        try
        {
            WebElement header = traitsElement.findElement(By.id(id));
            for (String foundPath : foundPathsLog4j)
            {
                WebElement property = header.findElement(By.xpath("//ul/li[text()='"+ foundPath +"']"));
                if (property != null)
                {
                    return true;
                }
            }
        } 
        catch (org.openqa.selenium.NoSuchElementException e) 
        {
            System.err.println("Element not found " + e.getLocalizedMessage());
        }
        return false;
    }

    private boolean isDependencyPropertyURLExists(WebElement traitsElement, String key, String value, String dependencyName)
    {
        String id = dependencyName+"-"+key;
        try
        {
            WebElement header = traitsElement.findElement(By.id(id));
            if (header != null)
            {
                WebElement url = traitsElement.findElement(By.partialLinkText(value));
                if (url != null)
                {
                    return true;
                }
            }
        }
        catch (org.openqa.selenium.NoSuchElementException e) 
        {
            System.err.println("Element not found " + e.getLocalizedMessage());
        }
        return false;
    }

    private boolean isDependencyPropertyExists(WebElement traitsElement, String key, String value, String dependencyName)
    {
        String id = dependencyName +"-"+key;
        try
        {
            WebElement header = traitsElement.findElement(By.id(id));
            if (header != null)
            {
                WebElement parent = header.findElement(By.xpath(".."));

                String parentText = parent.getText();
                if (parentText != null && parentText.endsWith(value))
                {
                    return true;
                }
            }
        }
        catch (org.openqa.selenium.NoSuchElementException e) 
        {
            System.err.println("Element not found " + e.getLocalizedMessage());
        }
        return false;
    }

}
