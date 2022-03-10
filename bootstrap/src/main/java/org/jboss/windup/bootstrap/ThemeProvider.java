package org.jboss.windup.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// We need duplicate this file and have it here as a Workaround for not being able
// to consume org.jboss.windup.util.* because of java.lang.NoClassDefFoundError in windup-distribution
// TODO: Fix this issue in https://issues.redhat.com/browse/WINDUP-3283 and then remove this block of code
public class ThemeProvider {

    private static volatile ThemeProvider instance;
    private final Theme theme;

    private ThemeProvider() {
        try (InputStream input = ThemeProvider.class.getClassLoader().getResourceAsStream("windup-config.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            String brandName = prop.getProperty("distributionBrandName");
            String nameAcronym = prop.getProperty("distributionBrandNameAcronym");
            String documentationUrl = prop.getProperty("distributionBrandDocumentationUrl");
            String cliName = prop.getProperty("distributionBrandCliName");

            theme = new Theme(brandName, nameAcronym, documentationUrl, cliName);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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
