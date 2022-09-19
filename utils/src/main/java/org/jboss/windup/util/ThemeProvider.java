package org.jboss.windup.util;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ThemeProvider {

    private static volatile ThemeProvider instance;
    private final Theme theme;

    private ThemeProvider() {
        try (InputStream input = ThemeProvider.class.getClassLoader().getResourceAsStream("windup-config.properties")) {
            if (input == null) {
                Furnace furnace = SimpleContainer.getFurnace(ThemeProvider.class.getClassLoader());
                try (InputStream another = furnace.getRuntimeClassLoader().getResourceAsStream("windup-config.properties")) {
                    theme = load(another);
                }
            } else {
                theme = load(input);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Theme load(InputStream input) throws IOException {
        Properties prop = new Properties();
        prop.load(input);

        final String brandName = prop.getProperty("distributionBrandName");
        final String nameAcronym = prop.getProperty("distributionBrandNameAcronym");
        final String websiteUrl = prop.getProperty("distributionBrandWebsiteUrl");
        final String documentationUrl = prop.getProperty("distributionBrandDocumentationUrl");
        final String cliName = prop.getProperty("distributionBrandCliName");

        return new Theme(brandName, nameAcronym, websiteUrl, documentationUrl, cliName);
    }

    public static ThemeProvider getInstance() {
        if (instance == null) {
            synchronized (ThemeProvider.class) {
                if (instance == null) {
                    instance = new ThemeProvider();
                }
            }
        }

        return instance;
    }

    public Theme getTheme() {
        return theme;
    }
}
