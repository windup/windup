package org.jboss.windup.bootstrap.help;

import java.util.List;

/**
 * Represents a potential parameter to the Windup engine.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class OptionDescription {
    private String name;
    private String description;
    private String type;
    private String uiType;
    private List<String> availableOptions;
    private boolean required;

    /**
     * Creates an {@link OptionDescription} with the specified name, description, type, and UI Type (select one, select many, etc).
     * <p>
     * The available options is unbounded.
     */
    public OptionDescription(String name, String description, String type, String uiType) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.uiType = uiType;
        this.availableOptions = null;
    }

    /**
     * Creates an {@link OptionDescription} with the specified name, description, type, UI Type (select one, select many, etc), and available option list.
     */
    public OptionDescription(String name, String description, String type, String uiType, List<String> availableOptions, boolean required) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.uiType = uiType;
        this.availableOptions = availableOptions;
        this.required = required;
    }

    /**
     * Gets the name of the option.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description of the option.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the type of the option.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the UI Type (SELECT_ONE, SELECT_MANY, etc).
     */
    public String getUiType() {
        return uiType;
    }

    /**
     * Gets a list of all available options.
     */
    public List<String> getAvailableOptions() {
        return availableOptions;
    }

    /**
     * Tells if the option is required.
     */
    public boolean isRequired() {
        return required;
    }
}
