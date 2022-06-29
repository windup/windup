package org.jboss.windup.testutil.html;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TestReportIndexReportUtil extends TestChromeDriverReportUtil {
    public boolean checkIncidentByCategoryRow(String category, int incidents, int totalStoryPoints) {
        WebElement element = getDriver().findElement(By.id("incidentsByTypeTable"));
        if (element == null) {
            throw new CheckFailedException("Unable to find incidents by type table element");
        }
        return checkValueInTable(element, category, String.valueOf(incidents), String.valueOf(totalStoryPoints));
    }

    /**
     * Loads the given page w/ the {@link WebDriver}
     */
    @Override
    public void loadPage(Path filePath) {
        try {
            Path modifiedPath = filePath.getParent().resolve(filePath.getFileName().toString() + "_modified.html");
            String contents = FileUtils.readFileToString(filePath.toFile());

            // remove some libraries that htmlunit has issues with... we don't really test these through htmlunit anyway
            contents = contents.replace("$.plot", "");
            // contents = contents.replace("<script src=\"resources/libraries/flot/jquery.flot.js\"></script>", "");
            contents = contents.replace("<script src=\"resources/libraries/flot/jquery.flot.js\"></script>",
                    "<script>$.plot = function(){}</script>");
            contents = contents.replace("<script src=\"resources/libraries/flot/jquery.flot.pie.min.js\"></script>", "");
            contents = contents.replace("<script src=\"resources/libraries/flot/jquery.flot.valuelabels.js\"></script>", "");
            contents = contents.replace("<script src=\"resources/libraries/flot/jquery.flot.axislabels.js\"></script>", "");
            contents = contents.replace("<script src=\"resources/libraries/flot/jquery.flot.resize.js\"></script>", "");

            contents = contents.replaceAll("jQuery.Color\\((.|[\n])*?\\);", "");

            contents = contents.replace("createTagCharts();", "");

            try (FileWriter writer = new FileWriter(modifiedPath.toFile())) {
                writer.append(contents);
            }

            getDriver().get(modifiedPath.toUri().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
