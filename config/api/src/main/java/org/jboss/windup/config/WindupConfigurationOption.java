package org.jboss.windup.config;

/**
 * Specifies details regarding a particular configuraiton option that can be passed into the Windup executor. This information is used by the UI to
 * determine the available options for running Windup.
 *
 * @author jsightler <jesse.sightler@gmail.com>
 * @author Ondrej Zizka
 */
public interface WindupConfigurationOption
{
    /**
     * Returns the name of the parameter. This should be a short name that is suitable for use in a command line parameter (for example, "packages" or
     * "excludePackages").
     */
    String getName();

    /**
     * Return a short amount of descriptive text regarding the option (for example, "Exclude Packages").
     */
    String getLabel();

    /**
     * Returns descriptive text that may be more lengthy and descriptive (for example, "Excludes the specified Java packages from Windup's scans").
     */
    String getDescription();

    /**
     * Returns the datatype for this Option (typically File, String, or List<String>).
     */
    Class<?> getType();

    /**
     * Returns a type that can be used as a hint to indicate what type of user interface should be presented for this option.
     */
    InputType getUIType();

    /**
     * Indicates whether or not this option must be specified.
     */
    boolean isRequired();

    /**
     * Default value for this option (if not set by user).
     */
    Object getDefaultValue();

    /**
     * Validate the user indicated value and return the result.
     */
    ValidationResult validate(Object value);

    /**
     * Indicates the "priority" of this option. Higher values (and therefore higher priority) of this value will result in the item being asked
     * earlier than items with a lower priority value.
     */
    int getPriority();
}
