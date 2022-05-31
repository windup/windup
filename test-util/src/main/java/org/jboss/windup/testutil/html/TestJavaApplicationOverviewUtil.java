package org.jboss.windup.testutil.html;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

/**
 * Contains methods for testing the Java Application Overview report.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TestJavaApplicationOverviewUtil extends TestChromeDriverReportUtil {

    /**
     * Loads the given page w/ the {@link WebDriver}
     */
    @Override
    public void loadPage(Path filePath) {
        try {
            Path modifiedPath = filePath.getParent().resolve(filePath.getFileName().toString() + "_modified.html");
            String contents = FileUtils.readFileToString(filePath.toFile(), Charset.defaultCharset());

            // remove some libraries that htmlunit has issues with... we don't really test these through htmlunit anyway
            contents = contents.replace("$.plot", "");
            contents = contents.replace("<script src=\"resources/libraries/flot/jquery.flot.min.js\"></script>",
                    "<script>$.plot = function(){}</script>");
            contents = contents.replace("<script src=\"resources/libraries/flot/jquery.flot.pie.min.js\"></script>", "");
            // RenderApplicationPieChartDirective
            contents = contents.replace("<script src=\"resources/js/jquery.color-2.1.2.min.js\"></script>",
                    "<script>jQuery.Color = function(){ return { toHexString: function(){ return \"#aa0000\"; } } }</script>");

            try (FileWriter writer = new FileWriter(modifiedPath.toFile())) {
                writer.append(contents);
            }

            getDriver().get(modifiedPath.toUri().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkApplicationMessage(String message) {
        // application-message
        List<WebElement> applicationMessageElements = getDriver()
                .findElements(By.className("application-message"));

        for (WebElement applicationMessageElement : applicationMessageElements) {
            if (message.equals(applicationMessageElement.getText()))
                return;
        }

        throw new CheckFailedException("Could not find an application message with text: " + message);
    }

    public void checkMainEffort(int expectedEffort) {
        WebElement effortElement = getDriver()
                .findElement(By.xpath(
                        ".//div[contains(@class, 'container') and contains(@class, 'mainGraphContainer')]//div[@class = 'points']/div[@class = 'number']"));
        String effortString = effortElement.getText().trim();
        effortString = effortString.replace(",", "");

        int effort;
        try {
            effort = Integer.parseInt(effortString);
        } catch (Exception e) {
            throw new CheckFailedException("Effort: " + effortString + " could not be parsed as numeric!");
        }
        if (effort != expectedEffort)
            throw new CheckFailedException("Effort was " + effort + " but was expected to be " + expectedEffort);
    }

    public void checkAppSectionEffort(String appSection, int expectedEffort) {
        WebElement appSectionEl = getAppSectionElement(appSection);
        if (appSectionEl == null) {
            throw new CheckFailedException("Unable to find app section with name: " + appSection);
        }

        String xpath = getElementXPath(getDriver(), appSectionEl) + "/"
                + "../..//div[@class = \\'points\\']/div[text() = \\'Story Points\\']/../div[@class = \\'number\\']";

        String effortString = getStringValueForXpathElement(getDriver(), xpath).trim();
        effortString = effortString.replace(",", "");

        try {
            int effort = Integer.parseInt(effortString);
            if (effort != expectedEffort)
                throw new CheckFailedException("Effort was " + effort + " but was expected to be " + expectedEffort);
        } catch (Exception e) {
            throw new CheckFailedException("Effort: " + effortString + " could not be parsed as numeric!");
        }
    }

    /**
     * Checks if the given App section, filepath, and effort level can be seen in the report.
     * <p>
     * For example checkFilePathEffort("src_example", "src/main/resources/test.properties", 13) will ensure that an application called "src_example"
     * is in the report, with a line referencing "src/main/resources/test.properties" and that this line contains the effort level 13).
     */
    public void checkFilePathEffort(String appSection, String filePath, int effort) {
        WebElement appSectionEl = getAppSectionElement(appSection);
        if (appSectionEl == null) {
            throw new CheckFailedException("Unable to find app section with name: " + appSection);
        }

        WebElement fileRowElement = getFileRowElement(appSection, filePath);
        if (fileRowElement == null) {
            throw new CheckFailedException("Unable to find row for filePath: " + filePath);
        }

        List<WebElement> elements = fileRowElement.findElements(By.xpath("./td[position() = 4]"));
        for (WebElement element : elements) {
            if (element.getText() != null) {
                try {
                    int number = Integer.parseInt(getTextForElement(element));
                    if (number == effort) {
                        return;
                    } else {
                        throw new CheckFailedException("Found row with appSection: " + appSection + " and filePath: "
                                + filePath
                                + ", but effort was: " + number + " (expected value: " + effort + ")");
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }

            }
        }
        throw new CheckFailedException("Unable to find app: " + appSection + " file: " + filePath + " with effort: "
                + effort);
    }

    /**
     * Checks if the given App section, filepath, and tag can be found in the report.
     * <p>
     * For example calling checkFilePathAndIssues("src_example", "src/main/resources/test.properties", "Web Servlet again") will ensure that an
     * application called "src_example" is in the report, with a line referencing "src/main/resources/test.properties" and that this line contains
     * text in the issues section saying "Web Servlet again").
     */
    public void checkFilePathAndIssues(String appSection, String filePath, String text) {
        WebElement appSectionEl = getAppSectionElement(appSection);
        if (appSectionEl == null) {
            throw new CheckFailedException("Unable to find app section with name: " + appSection);
        }

        WebElement fileRowElement = getFileRowElement(appSection, filePath);
        if (fileRowElement == null) {
            throw new CheckFailedException("Unable to find row for filePath: " + filePath);
        }

        List<WebElement> elements = fileRowElement.findElements(By.xpath("./td[position() = 3]"));
        for (WebElement element : elements) {
            String elementText = getTextForElement(element);
            if (elementText != null && elementText.contains(text)) {
                return;
            }
        }

        throw new CheckFailedException("Unable to find app: " + appSection + " file: " + filePath + " with issue: "
                + text);
    }

    /**
     * Checks if the given App section, filepath, and tag can be found in the report.
     * <p>
     * For example calling checkFilePathAndTag("src_example", "src/main/resources/test.properties", "Properties") will ensure that an application
     * called "src_example" is in the report, with a line referencing "src/main/resources/test.properties" and that this file is tagged "Properties"
     */
    public void checkFilePathAndTag(String appSection, String filePath, String tag) {
        WebElement appSectionEl = getAppSectionElement(appSection);
        if (appSectionEl == null) {
            throw new CheckFailedException("Unable to find app section with name: " + appSection);
        }

        WebElement fileRowElement = getFileRowElement(appSection, filePath);
        if (fileRowElement == null) {
            throw new CheckFailedException("Unable to find row for filePath: " + filePath);
        }

        List<WebElement> elements = fileRowElement.findElements(By.xpath("./td[position() = 2]/span"));
        for (WebElement element : elements) {
            String spanValue = getTextForElement(element);
            if (spanValue.equals(tag)) {
                return;
            }
        }

        throw new CheckFailedException("Unable to find app: " + appSection + " file: " + filePath + " with tag: "
                + tag);
    }

    /**
     * In case the element's css is display:none, selenium does not see it using getText(). Therefore this methods uses javascript to query the value
     *
     * @param element
     * @return
     */
    private String getTextForElement(WebElement element) {
        String xpath = getElementXPath(driver, element);
        String result = getStringValueForXpathElement(driver, xpath);
        return result.trim();
    }

    /**
     * Returns the xpath full path of the given element. E.g something like /html/body/div[2]/p
     *
     * @param driver
     * @param element
     * @return
     */
    private String getElementXPath(WebDriver driver, WebElement element) {
        String xpath = (String) ((JavascriptExecutor) driver).executeScript(
                "gPt=function(c){if(c.id!==''){return'id(\"'+c.id+'\")'}if(c===document.body){return c.tagName}var a=0;var e=c.parentNode.childNodes;for(var b=0;b<e.length;b++){var d=e[b];if(d===c){return gPt(c.parentNode)+'/'+c.tagName+'['+(a+1)+']'}if(d.nodeType===1&&d.tagName===c.tagName){a++}}};return gPt(arguments[0]).toLowerCase();",
                element);
        if (!StringUtils.startsWith(xpath, "id(\""))
            xpath = "/html/" + xpath;
        return xpath;
    }

    private String getStringValueForXpathElement(WebDriver driver, String xpathToElement) {
        return (String) ((JavascriptExecutor) driver).executeScript("var foundDocument=document.evaluate( '" + xpathToElement
                + "', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null ).singleNodeValue; if(foundDocument !=null) {return foundDocument.textContent;} else {return null} ");
    }

    /**
     * Checks if the given App section, filepath, and tag can be found in the report.
     * <p>
     * For example calling checkFilePathAndTag("src_example", "src/main/resources/test.properties") will ensure that an application called
     * "src_example" is in the report, with a line referencing "src/main/resources/test.properties"
     */
    public void checkFilePath(String appSection, String filePath) {
        WebElement appSectionEl = getAppSectionElement(appSection);
        if (appSectionEl == null) {
            throw new CheckFailedException("Unable to find app section with name: " + appSection);
        }

        WebElement fileRowElement = getFileRowElement(appSection, filePath);
        if (fileRowElement == null) {
            throw new CheckFailedException("Unable to find row for filePath: " + filePath);
        }

        return;
    }

    private WebElement getFileRowElement(String appSection, String filePath) {
        WebElement fileTable = getAppSectionElement(appSection).findElement(By.xpath("../../div[contains(@class,'panel-body')]/table"));

        WebElement fileRow = fileTable.findElement(By
                .xpath("./tbody/tr/td/a[normalize-space(text()) = '" + filePath + "']/../.."));
        return fileRow;
    }

    private WebElement getAppSectionElement(String appSection) {
        List<WebElement> titleElements = getDriver().findElements(By.className("panel-title"));
        for (WebElement el : titleElements) {
            String panelTitleText = el.getText();
            if (panelTitleText != null) {
                panelTitleText = parseOutAppTitle(panelTitleText);
                if (appSection.equals(panelTitleText.trim())) {
                    return el;
                }
            }

        }
        return null;
    }

    private String parseOutAppTitle(String input) {
        // remove story points information
        return input.replaceAll("\\(.*\\)", "");
    }
}
