package org.jboss.windup.testutil.html;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Contains methods for evaluating and retrieving data from the technologies report.
 *
 * @author Ondrej Zizka
 */
public class TestTechReportUtil extends TestReportUtil
{
    /**
     * Basically this checks few bubbles if they are as big (or missing) as they should be.
     */
    public void checkTechGlobalReport(Path path, List<BubbleInfo> bubblesExpected)
    {
        loadPage(path);

        for (BubbleInfo bubbleInfo : bubblesExpected)
        {
            checkBubble(bubbleInfo);
        }

        this.getDriver().close();
    }

    /**
     * The sizes are 0 to 4. 0 represents the missing icon, 1-4 are the sizes used.
     */
    private void checkBubble(BubbleInfo bubbleExpected)
    {
        String appName = bubbleExpected.appName.trim();
        int colOffset = getPunchCardReportColumnOffset(bubbleExpected.techColumnLabel);
        final String xpath = String.format("tr[@class='app' and td/a[normalize-space()='%s']]/td[position()=%d]", appName, colOffset);
        WebElement bubbleCell = getDriver().findElement(By.xpath(xpath));
        if (bubbleCell == null)
            throw new CheckFailedException(String.format("Bubble cell not found for app %s and bubble %s", appName, bubbleExpected.techColumnLabel));

        int actualSize = parseSizeFromClasses(bubbleCell.getAttribute("class"));

        if (actualSize < bubbleExpected.minSize)
            throw new CheckFailedException(String.format("Bubble size smaller than expected: %d < %d", actualSize, bubbleExpected.minSize));
        if (actualSize > bubbleExpected.maxSize)
            throw new CheckFailedException(String.format("Bubble size larger than expected: %d > %d", actualSize, bubbleExpected.maxSize));
    }

    private int parseSizeFromClasses(String classes)
    {
        final String size = Pattern.compile("(?:.*size)\\d(?:.*)").matcher(classes).group(0);
        return Integer.valueOf(size);
    }

    private int getPunchCardReportColumnOffset(String label){
        label = label.trim();
        WebElement headers = getDriver().findElement(By.xpath(String.format("//tr[@class='headersGroup']", label)));
        assertNotNull(headers, "headers row", true);

        WebElement header = getDriver().findElement(By.xpath(String.format("//tr[@class='headersGroup']/td/div[normalize-space()='%s']", label)));
        assertNotNull(headers, "header column div " + label, true);

        final String xpath = String.format("//tr[@class='headersGroup']/td[div[normalize-space()='%s']]/preceding-sibling::td", label);
        List<WebElement> precedingSiblings = getDriver().findElements(By.xpath(xpath));
        assertNotNull(headers, "precending siblings of header column div " + label, true);

        return precedingSiblings.size() -1;
    }

    // ----------------------------

    public void checkTechBoxReport(Path path, List<BoxInfo> boxesExpected)
    {
        loadPage(path);

        for (BoxInfo box : boxesExpected)
        {
            checkBox(box);
        }

        this.getDriver().close();
    }

    private void checkBox(BoxInfo boxExpected)
    {
        final String xpath = String.format("//div[contains(@class,'box') and //h4[normalize-space()='%s']]", boxExpected.boxLabel);
        WebElement box = getDriver().findElement(By.xpath(xpath));
        if (box == null)
            throw new CheckFailedException(
                    String.format("Box '%s' not found for row '%s' and sector '%s'",
                    boxExpected.boxLabel, boxExpected.rowLabel, boxExpected.sectorLabel));

        WebElement techLi = box.findElement(By.xpath(String.format("//ul/li[contains(normalize-space(), '%s')]", boxExpected.techName)));
        if (techLi == null)
            throw new CheckFailedException(
                    String.format("Tech '%s' not found in box '%s' at row '%s' and under sector '%s'",
                    boxExpected.techName, boxExpected.boxLabel, boxExpected.rowLabel, boxExpected.sectorLabel));

        if (boxExpected.count == 0)
            return;

        final Integer actualCount = Integer.valueOf(techLi.findElement(By.name("b")).getText());
        if (actualCount != boxExpected.count)
            throw new CheckFailedException(
                    String.format("Count %d expected to be %d for tech '%s' not found in box '%s' at row '%s' and under sector '%s'",
                    actualCount, boxExpected.count, boxExpected.techName, boxExpected.boxLabel, boxExpected.rowLabel, boxExpected.sectorLabel));
    }



    private boolean assertNotNull(Object object, String whatIsIt, boolean throwIfNotFound)
    {
        if (object == null) {
            if (throwIfNotFound)
                throw new CheckFailedException(whatIsIt + " was not found on the page.");
        }
        return object != null;
    }




    public static class BubbleInfo
    {
        String appName;
        String techColumnLabel;
        int minSize;
        int maxSize;

        public BubbleInfo(String appName, String techColumnLabel, int minSize, int maxSize)
        {
            this.appName = appName;
            this.techColumnLabel = techColumnLabel;
            this.minSize = minSize;
            this.maxSize = maxSize;
        }
    }

    public static class BoxInfo
    {
        String rowLabel;
        String sectorLabel;
        String boxLabel;
        String techName;
        int count; // Will be missing in HTML if 0.

        /**
         * @param sectorLabel  If null, the row should not contain any boxes.
         * @param boxLabel     If null, the sector should not contain any boxes.
         * @param count        If null, the sector should not contain any boxes.
         */
        public BoxInfo(String rowLabel, String sectorLabel, String boxLabel, String techName, int count)
        {
            this.rowLabel = rowLabel;
            this.sectorLabel = sectorLabel;
            this.boxLabel = boxLabel;
            this.techName = techName;
            this.count = count;
        }
    }

}
