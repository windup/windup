package org.jboss.windup.testutil.html;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Contains utility methods for assisting tests in interacting with the generated reports using Chrome Driver.
 *
 * @author mrizzi
 */
public class TestChromeDriverReportUtil extends TestReportUtil {
    public TestChromeDriverReportUtil() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        this.driver = new ChromeDriver(chromeOptions);
    }

    @Override
    public void loadPage(Path filePath) {
        try {
            if (!filePath.toFile().exists())
                throw new CheckFailedException("Requested page file does not exist: " + filePath);
            driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
            driver.get(filePath.toUri().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
