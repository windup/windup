package org.jboss.windup.rules.apps.java.scan.ast;

/**
 * Designates a location where a given {@link JavaTypeReferenceModel} was found in a Java source file.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public enum TypeReferenceLocation
{
    IMPORT("Import of"),
    TYPE("Declares type"),
    METHOD("Declares method"),
    INHERITANCE("Inherits type"),
    CONSTRUCTOR_CALL("Constructing type"),
    METHOD_CALL("Calls method"),
    METHOD_PARAMETER("Method parameter"),
    ANNOTATION("References annotation"),
    RETURN_TYPE("Returns type"),
    INSTANCE_OF("Instance of type"),
    THROWS_METHOD_DECLARATION("Throws"),
    THROW_STATEMENT("Throw"),
    CATCH_EXCEPTION_STATEMENT("Catches exception"),
    FIELD_DECLARATION("Declares field"),
    VARIABLE_DECLARATION("Declares variable"),
    IMPLEMENTS_TYPE("Implements type");

    private String readablePrefix;

    private TypeReferenceLocation(String readablePrefix)
    {
        this.readablePrefix = readablePrefix;
    }

    /**
     * Returns the enumeration as a human readable value.
     */
    public String toReadablePrefix()
    {
        return this.readablePrefix;
    }
}
