package org.jboss.windup.testutil.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests the contents of the compatible files report
 */
public class TestCompatibleReportUtil extends TestChromeDriverReportUtil {

    /**
     * Checks that a sis listed with the given name and technology
     */
    public boolean checkFileInReport(String name, String technology) {
        List<WebElement> elements = getDriver().findElements(By.tagName("table"));
        if (elements == null || elements.isEmpty()) {
            throw new CheckFailedException("Unable to find any static Compatible table element");
        }
        boolean result = false;
        for (WebElement el : elements) {
            result |= checkValueInTable(el, name, technology);
            if (result) break;
        }
        return result;
    }

    /**
     * Checks that a sis listed with the given name and technology
     */
    public boolean checkTableWithoutDuplicates() {

        List<WebElement> elements = getDriver().findElements(By.tagName("table"));
        if (elements == null || elements.isEmpty()) {
            throw new CheckFailedException("Unable to find any static Compatible table element");
        }
        for (WebElement element : elements) {
            //for a single table
            Set<String> foundClasses = new HashSet<>();
            List<WebElement> rowElements = element.findElements(By.xpath(".//tr"));
            for (WebElement rowElement : rowElements) {
                List<WebElement> tdElements = rowElement.findElements(By.xpath(".//td[position() = " + (1) + "]"));
                if (tdElements.size() != 1) {
                    break;
                }
                String firstElementString = tdElements.get(0).getText().trim();
                String withoutExtension = firstElementString.replaceAll("\\.class", "");
                withoutExtension = withoutExtension.replaceAll("\\.java", "");
                if (foundClasses.contains(withoutExtension)) {
                    return false;
                } else {
                    foundClasses.add(withoutExtension);
                }

            }
        }
        return true;
    }
}
