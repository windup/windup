package org.jboss.windup.testutil.html;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Contains utility methods for assisting tests in interacting with the generated reports.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author Ondrej Zizka
 */
public class TestReportUtil {
    private final static Logger LOG = Logger.getLogger(TestReportUtil.class.getName());
    protected WebDriver driver;

    public TestReportUtil() {
        this.driver = new WindupHtmlUnitDriver(BrowserVersion.FIREFOX, true);
    }

    /**
     * Loads the given page w/ the {@link WebDriver}
     */
    public void loadPage(Path filePath) {
        LOG.info("Loading page: " + filePath);
        if (!filePath.toFile().exists())
            throw new CheckFailedException("Requested page file does not exist: " + filePath);
        getDriver().get(filePath.toUri().toString());
    }

    protected WebDriver getDriver() {
        return driver;
    }

    /**
     * Checks that the table contains a row with the given first two columns
     */
    boolean checkValueInTable(WebElement element, String... columnValues) {
        List<WebElement> rowElements = element.findElements(By.xpath(".//tr"));
        boolean foundExpectedResult = false;
        for (WebElement rowElement : rowElements) {
            boolean rowMatches = true;
            for (int i = 0; i < columnValues.length; i++) {
                String expectedValue = columnValues[i];
                List<WebElement> tdElements = rowElement.findElements(By.xpath("./td[position() = " + (i + 1) + "]"));
                if (tdElements.size() != 1) {
                    rowMatches = false;
                    break;
                }
                String actualValue = tdElements.get(0).getText().trim();
                if (!actualValue.trim().equals(expectedValue.trim())) {
                    rowMatches = false;
                    break;
                }
            }
            if (rowMatches) {
                foundExpectedResult = true;
                break;
            }
        }
        return foundExpectedResult;
    }


    public class WindupHtmlUnitDriver extends HtmlUnitDriver {

        public WindupHtmlUnitDriver(BrowserVersion version, boolean enableJavascript) {
            super(version, enableJavascript);
        }

        @Override
        protected WebClient modifyWebClient(WebClient client) {
            WebClient modifiedClient = super.modifyWebClient(client);
            modifiedClient.getOptions().setThrowExceptionOnScriptError(false);
            modifiedClient.setCssErrorHandler(new SilentCssErrorHandler());
            return modifiedClient;
        }
    }
}
