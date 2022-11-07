package org.jboss.windup.util;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.forge.furnace.versions.EmptyVersion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

        final String topBarTitle = prop.getProperty("distributionTopBarTitle");
        final String brandName = prop.getProperty("distributionBrandName");
        final String nameAcronym = prop.getProperty("distributionBrandNameAcronym");
        final String websiteUrl = prop.getProperty("distributionBrandWebsiteUrl");
        final String documentationUrl = prop.getProperty("distributionBrandDocumentationUrl");
        final String cliName = prop.getProperty("distributionBrandCliName");

        // this file is available in the home folder of the CLI only
        final File cliVersionFile = PathUtil.getWindupHome().resolve("cli-version.txt").toFile();
        String cliVersion = getRuntimeAPIVersion();
        if (cliVersionFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(cliVersionFile))) {
                cliVersion = br.readLine();
            } catch (Exception e) {
                // do nothing because the execution continues with the fallback method below
            }
        }
        return new Theme(topBarTitle, brandName, nameAcronym, websiteUrl, documentationUrl, cliName, cliVersion, getRuntimeAPIVersion());
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

    private String getRuntimeAPIVersion() {
        String version = ThemeProvider.class.getPackage().getImplementationVersion();
        if (version != null) {
            return version;
        }
        return EmptyVersion.getInstance().toString();
    }

}
