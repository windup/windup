package org.jboss.windup.config;

/**
 * Indicates the type of UI element that should be used for this input.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public enum InputType
{
    /**
     * A single input value (eg, a String with a simple textbox).
     */
    SINGLE("Single"),
    /**
     * Select multiple values (eg, List<String>).s
     */
    MANY("Many"),
    /**
     * Select one item from a List.
     */
    SELECT_ONE("Select One"),
    /**
     * Select many items from a List.
     */
    SELECT_MANY("Select Many"),
    /**
     * Select a File.
     */
    FILE("File"),
    /**
     * Select a Directory.
     */
    DIRECTORY("Directory"),
    /**
     * Select either a File or a Directory.
     */
    FILE_OR_DIRECTORY("File or Directory");

    private String value;

    private InputType(String val)
    {
        this.value = val;
    }

    @Override
    public String toString()
    {
        return value;
    }
}
