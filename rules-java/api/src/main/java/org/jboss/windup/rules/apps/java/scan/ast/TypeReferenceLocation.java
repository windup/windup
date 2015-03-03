package org.jboss.windup.rules.apps.java.scan.ast;

/**
 * Designates a location where a given {@link JavaTypeReferenceModel} was found in a Java source file.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public enum TypeReferenceLocation
{
    /**
     *  A Java class imports the type.
     */
    IMPORT("Import of"),
    /**
     * A Java class declares the type.
     */
    TYPE("Declares type"),
    /**
     * A Java class declares the referenced method.
     */
    METHOD("Declares method"),
    /**
     * A Java class inherits the type reference.
     */
    INHERITANCE("Inherits type"),
    /**
     * A Java class constructs the type reference.
     */
    CONSTRUCTOR_CALL("Constructing type"),
    /**
     * A Java class calls the referenced method.
     */
    METHOD_CALL("Calls method"),
    /**
     * A Java class declares the referenced method parameter.
     */
    METHOD_PARAMETER("Method parameter"),
    /**
     * A Java class references the annotation.
     */
    ANNOTATION("References annotation"),
    /**
     * A Java class returns the type reference.
     */
    RETURN_TYPE("Returns type"),
    /**
     * A Java class is an instance of the type reference.
     */
    INSTANCE_OF("Instance of type"),
    /**
     * A Java class declares it throws the type reference.
     */
    THROWS_METHOD_DECLARATION("Throws"),
    /**
     * A method in the Java class throws the type reference.
     */
    THROW_STATEMENT("Throw"),
    /**
     * A Java class method catches the type reference.
     */
    CATCH_EXCEPTION_STATEMENT("Catches exception"),
    /**
     * A Java class declares a field of the type reference.
     */
    FIELD_DECLARATION("Declares field"),
    /**
     * A Java class declares a variable of the type reference.
     */
    VARIABLE_DECLARATION("Declares variable"),
    /**
     * A Java class implements the type reference.
     */
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
