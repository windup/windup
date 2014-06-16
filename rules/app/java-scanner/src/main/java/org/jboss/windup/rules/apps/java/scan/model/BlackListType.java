package org.jboss.windup.rules.apps.java.scan.model;

public enum BlackListType
{
    ANNOTATION,
    IMPORT,
    INSTANCE_OF_CHECK,
    METHOD_CALL,
    EXTENDS_TYPE,
    IMPLEMENTS_TYPE,
    DEFINES_TYPE,
    REFERENCES_TYPE,
    REFERENCES_EXCEPTION_OF_TYPE,
    DECLARES_FIELD_OF_TYPE,
    DECLARES_VARIABLE_OF_TYPE,
    OTHER
}
