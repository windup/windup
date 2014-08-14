package org.jboss.windup.rules.apps.java.scan.ast;

/**
 * Designates a location where a given {@link TypeReferenceModel} was found in a Java source file.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public enum TypeReferenceLocation
{
    IMPORT,
    NOTSPECIFIED,
    TYPE,
    METHOD,
    INHERITANCE,
    CONSTRUCTOR_CALL,
    METHOD_CALL,
    METHOD_PARAMETER,
    ANNOTATION,
    RETURN_TYPE,
    INSTANCE_OF,
    THROWS_METHOD_DECLARATION,
    THROW_STATEMENT,
    CATCH_EXCEPTION_STATEMENT,
    FIELD_DECLARATION,
    VARIABLE_DECLARATION,
    IMPLEMENTS_TYPE,
    EXTENDS_TYPE
}
