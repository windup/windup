package org.jboss.windup.testutil.html;

import java.nio.file.Path;
import java.time.Duration;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Contains utility methods for assisting tests in interacting with the generated reports using Chrome Driver.
 *
 * @author mrizzi
 */
public class TestChromeDriverReportUtil extends TestReportUtil {
    public TestChromeDriverReportUtil() {
        // This is the "right" solution https://www.selenium.dev/blog/2022/using-java11-httpclient/
        // but it generated a ClassNotFoundException: java.net.http.HttpTimeoutException from JBoss Modules
        // due to https://issues.redhat.com/browse/MODULES-392 and
        // org.jboss.forge.furnace:furnace:2.29.1.Final using org.jboss.modules:jboss-modules:1.9.1.Final
//        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("webdriver.http.factory", "netty");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless=new");
        chromeOptions.addArguments("--remote-allow-origins=*");
        this.driver = new ChromeDriver(chromeOptions);
    }

    @Override
    public void loadPage(Path filePath) {
        try {
            if (!filePath.toFile().exists())
                throw new CheckFailedException("Requested page file does not exist: " + filePath);
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
            driver.get(filePath.toUri().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
