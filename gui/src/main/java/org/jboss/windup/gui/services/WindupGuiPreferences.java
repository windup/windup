package org.jboss.windup.gui.services;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.windup.gui.WindupGuiCommand;

/**
 * Stores the user's preferences that specifically relate to the Windup GUI. This could include things like the last selected directory or Window
 * positions.
 * 
 * This should not be used as a general store for Windup related configuration.
 * 
 * @author jsightler
 *
 */
public class WindupGuiPreferences
{
    private static final String LAST_SELECTED_INPUT_PATH = "lastSelectedInputPath";
    private static final String LAST_SELECTED_OUTPUT_PATH = "lastSelectedOutputPath";

    private static Path getUserHome()
    {
        return OperatingSystemUtils.getUserHomeDir().toPath();
    }

    /**
     * Gets the most recent input path that was selected by the user.
     */
    public static Path getLastSelectedInputPath()
    {
        Preferences prefs = Preferences.userNodeForPackage(WindupGuiCommand.class);
        String str = prefs.get(LAST_SELECTED_INPUT_PATH, getUserHome().normalize().toString());
        return Paths.get(str);
    }

    /**
     * Sets the most recent input path that was selected by the user.
     */
    public static void setLastSelectedInputPath(Path path)
    {
        Preferences prefs = Preferences.userNodeForPackage(WindupGuiCommand.class);
        prefs.put(LAST_SELECTED_INPUT_PATH, path.normalize().toString());
    }

    /**
     * Gets the most recent output path that was selected by the user.
     */
    public static Path getLastSelectedOutputPath()
    {
        Preferences prefs = Preferences.userNodeForPackage(WindupGuiCommand.class);
        String str = prefs.get(LAST_SELECTED_OUTPUT_PATH, getUserHome().normalize().toString());
        return Paths.get(str);
    }

    /**
     * Sets the most recent output path that was selected by the user.
     */
    public static void setLastSelectedOutputPath(Path path)
    {
        Preferences prefs = Preferences.userNodeForPackage(WindupGuiCommand.class);
        prefs.put(LAST_SELECTED_OUTPUT_PATH, path.normalize().toString());
    }
}
