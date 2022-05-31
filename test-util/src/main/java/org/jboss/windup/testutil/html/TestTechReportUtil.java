package org.jboss.windup.testutil.html;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.util.Util;
import org.ocpsoft.common.util.Strings;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains methods for evaluating and retrieving data from the technologies report.
 *
 * @author Ondrej Zizka
 */
public class TestTechReportUtil extends TestChromeDriverReportUtil {
    private final static Logger LOG = Logger.getLogger(TestReportUtil.class.getName());

    final int COLS_BEFORE_BUBBLES = 1;

    /**
     * Basically this checks few bubbles if they are as big (or missing) as they should be.
     */
    public void checkTechGlobalReport(Path path, List<BubbleInfo> bubblesExpected) {
        loadPage(path);

        for (BubbleInfo bubbleInfo : bubblesExpected) {
            checkBubble(bubbleInfo);
        }

        this.getDriver().close();
    }

    public void checkPoints(String appName, PointsType pointsType, int expectedCount) {
        LOG.info("    Checking points " + appName + ", pointstype: " + pointsType);

        String columnName;
        switch (pointsType) {
            case MANDATORY:
                columnName = "Mandatory (SP)";
                break;
            case POTENTIAL:
                columnName = "Potential (Count)";
                break;
            case CLOUD_MANDATORY:
                columnName = "Cloud Mandatory (SP)";
                break;
            default:
                throw new CheckFailedException("Unrecognized type: " + pointsType);
        }

        int colOffset = getPunchCardReportColumnOffset(columnName);
        final String precedingSummaryElementXpath = String.format("//tr[@class='app' and td/a[normalize-space()='%s']]/td[contains(@class, 'sectorSummary')]",
                appName);
        List<WebElement> precedingSummaryElements = getDriver().findElements(By.xpath(precedingSummaryElementXpath));
        //No '+1' in this colOffset as the 'name' field gets counted twice
        final String xpath = String.format("//tr[@class='app' and td/a[normalize-space()='%s']]/td[position()=%d]",
                appName, colOffset + COLS_BEFORE_BUBBLES + precedingSummaryElements.size());
        List<WebElement> bubbleCells = getDriver().findElements(By.xpath(xpath));
        if (bubbleCells.isEmpty())
            throw new CheckFailedException(String.format("Cell not found for app %s and column %s;  xpath: " + xpath, appName, pointsType));
        WebElement cell = bubbleCells.get(0);

        String cellText = cell.getText();
        if (StringUtils.isBlank(cellText))
            throw new CheckFailedException("No contents found for " + appName + " points type: " + pointsType);

        cellText = cellText.trim();

        int count = Integer.parseInt(cellText);
        if (expectedCount != count)
            throw new CheckFailedException("For " + appName + " points type: " + pointsType + ", expected: " + expectedCount + ", but actual value was: " + count);
    }

    /**
     * The sizes are 0 to 4. 0 represents the missing icon, 1-4 are the sizes used.
     */
    private void checkBubble(BubbleInfo bubbleExpected) {
        LOG.info("    Checking bubble " + bubbleExpected);

        String appName = bubbleExpected.appName.trim();
        int colOffset = getPunchCardReportColumnOffset(bubbleExpected.techColumnLabel);
        final String precedingSummaryElementXpath = String.format("//tr[@class='app' and td/a[normalize-space()='%s']]/td[contains(@class, 'sectorSummary') and position()<=%d]",
                appName, colOffset + COLS_BEFORE_BUBBLES + 1);
        List<WebElement> precedingSummaryElements = getDriver().findElements(By.xpath(precedingSummaryElementXpath));
        //No '+1' in this colOffset as the 'name' field gets counted twice
        final String xpath = String.format("//tr[@class='app' and td/a[normalize-space()='%s']]/td[position()=%d]",
                appName, colOffset + COLS_BEFORE_BUBBLES + precedingSummaryElements.size());
        List<WebElement> bubbleCells = getDriver().findElements(By.xpath(xpath));
        if (bubbleCells.isEmpty())
            throw new CheckFailedException(String.format("Bubble cell not found for app %s and column %s;  xpath: " + xpath, appName, bubbleExpected.techColumnLabel));
        WebElement bubbleCell = bubbleCells.get(0);

        int actualSize = parseSizeFromClasses(bubbleCell.getAttribute("class"));

        if (actualSize < bubbleExpected.minSize)
            throw new CheckFailedException(String.format("Bubble size smaller than expected: %d < %d", actualSize, bubbleExpected.minSize));
        if (actualSize > bubbleExpected.maxSize)
            throw new CheckFailedException(String.format("Bubble size larger than expected: %d > %d", actualSize, bubbleExpected.maxSize));
    }

    private int parseSizeFromClasses(String classes) {
        Matcher mat = Pattern.compile("(?:.*size)(\\d)(?:.*)").matcher(classes);
        mat.matches();
        String size = mat.group(1);
        return Integer.valueOf(size);
    }

    private int getPunchCardReportColumnOffset(String label) {
        label = label.trim();
        List<WebElement> headersTR = getDriver().findElements(By.xpath(String.format("//tr[@class='headersGroup']", label)));
        assertNotEmpty(headersTR, "headers row");

        List<WebElement> headerDIV = getDriver().findElements(By.xpath(String.format("//tr[@class='headersGroup']/td/div[normalize-space()='%s']", label)));
        assertNotEmpty(headerDIV, "header column div " + label);

        final String xpath = String.format("//tr[@class='headersGroup']/td[div[normalize-space()='%s']]/preceding-sibling::td", label);
        List<WebElement> precedingSiblings = getDriver().findElements(By.xpath(xpath));
        assertNotEmpty(precedingSiblings, "precending siblings of header column div " + label);


        return precedingSiblings.size() - COLS_BEFORE_BUBBLES; // Substracting the app name column.
    }

    // ----------------------------

    public void checkTechBoxReport(Path path, List<BoxInfo> boxesExpected) {
        loadPage(path);

        List errors = new ArrayList();
        for (BoxInfo box : boxesExpected) {
            if (box.sectorLabel == null)
                checkNoBoxInRow(box);
            else if (box.boxLabel == null)
                //checkNoBoxUnderSector(box); // TBD when someone wants to.
                ;
            else {
                String error = checkBox(box);
                if (null != error)
                    errors.add(error);
            }
        }
        String glue = Util.NL + "  * ";
        if (!errors.isEmpty())
            throw new CheckFailedException("Tech report is wrong: " + glue + Strings.join(errors, glue));

        this.getDriver().close();
    }

    private void checkNoBoxInRow(BoxInfo boxNotExpected) {
        final String xpath = String.format("//tr[@class='rowHeader' and //div[normalize-space()='%s']]//div[contains(@class,'box')]", boxNotExpected.rowLabel);
        List<WebElement> boxes = getDriver().findElements(By.xpath(xpath));
        if (!boxes.isEmpty())
            throw new CheckFailedException(
                    String.format("There should be no boxes for row '%s';  xpath: " + xpath, boxNotExpected.rowLabel));
    }

    private void checkNoBoxUnderSector(BoxInfo boxNotExpected) {
        int sectorOffset = getBoxesReportSectorOffset(boxNotExpected.sectorLabel);

        final String xpath = String.format("//tr[@class='rowHeader' and //div[normalize-space()='%s']]//td[position()=%d]//div[contains(@class,'box')", boxNotExpected.rowLabel, sectorOffset);
        List<WebElement> boxes = getDriver().findElements(By.xpath(xpath));
        if (!boxes.isEmpty())
            throw new CheckFailedException(
                    String.format("There should be no boxes for row '%s' and sector '%s';  xpath: " + xpath,
                            boxNotExpected.rowLabel, boxNotExpected.sectorLabel));
    }

    private int getBoxesReportSectorOffset(String sectorLabel) {
        // TBD
        return 0;
    }

    /**
     * Returns null if ok, or an error message if not found or wrong number.
     */
    private String checkBox(BoxInfo boxExpected) {
        final String xpath = String.format("//div[contains(@class,'box') and //h4[normalize-space() = '%s']]", boxExpected.boxLabel);
        List<WebElement> boxes = getDriver().findElements(By.xpath(xpath));
        if (boxes.isEmpty())
            return String.format("Box '%s' not found for row '%s' and sector '%s';  xpath: " + xpath,
                    boxExpected.boxLabel, boxExpected.rowLabel, boxExpected.sectorLabel);

        List<WebElement> techItems = boxes.get(0).findElements(By.xpath(String.format("//ul/li[node()[normalize-space() = '%s']]", boxExpected.techName)));
        if (techItems.isEmpty())
            return String.format("Tech '%s' not found in box '%s' at row '%s' and under sector '%s'",
                    boxExpected.techName, boxExpected.boxLabel, boxExpected.rowLabel, boxExpected.sectorLabel);

        if (boxExpected.minCount == 0)
            return null;

        WebElement techLi = techItems.get(0);
        final List<WebElement> countBs = techLi.findElements(By.tagName("b"));
        if (countBs.isEmpty())
            return String.format("Count was missing, expected to be %d for tech '%s' not found in box '%s' at row '%s' and under sector '%s'",
                    boxExpected.minCount, boxExpected.techName, boxExpected.boxLabel, boxExpected.rowLabel, boxExpected.sectorLabel);

        final Integer actualCount = Integer.valueOf(countBs.get(0).getText());
        if (actualCount < boxExpected.minCount)
            return String.format("Count was %d, expected to be at least %d for tech '%s' not found in box '%s' at row '%s' and under sector '%s'",
                    actualCount, boxExpected.minCount, boxExpected.techName, boxExpected.boxLabel, boxExpected.rowLabel, boxExpected.sectorLabel);

        return null;
    }


    private boolean assertNotNull(Object object, String whatIsIt, boolean throwIfNotFound) {
        if (object == null) {
            if (throwIfNotFound)
                throw new CheckFailedException(whatIsIt + " was not found on the page.");
        }
        return object != null;
    }

    private void assertNotEmpty(Iterable iterable, String whatIsIt) {
        if (!iterable.iterator().hasNext()) {
            throw new CheckFailedException(whatIsIt + " was not found on the page.");
        }
    }


    public enum PointsType {
        MANDATORY,
        CLOUD_MANDATORY,
        POTENTIAL
    }

    public static class BubbleInfo {
        String appName;
        String techColumnLabel;
        int minSize;
        int maxSize;

        public BubbleInfo(String appName, String techColumnLabel, int minSize, int maxSize) {
            this.appName = appName;
            this.techColumnLabel = techColumnLabel;
            this.minSize = minSize;
            this.maxSize = maxSize;
        }

        @Override
        public String toString() {
            return "BubbleInfo{appName='" + appName + '\'' + ", techColumnLabel='" + techColumnLabel + '\'' + ", " + minSize + " to " + maxSize + '}';
        }
    }

    public static class BoxInfo {
        String rowLabel;
        String sectorLabel;
        String boxLabel;
        String techName;
        int minCount; // The count will be missing in HTML if 0.
        int maxCount;

        /**
         * @param sectorLabel If null, the row should not contain any boxes.
         * @param boxLabel    If null, the sector should not contain any boxes.
         */
        public BoxInfo(String rowLabel, String sectorLabel, String boxLabel, String techName, int minCount, int maxCount) {
            this.rowLabel = rowLabel;
            this.sectorLabel = sectorLabel;
            this.boxLabel = boxLabel;
            this.techName = techName;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }
    }

}
