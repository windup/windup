package org.jboss.windup.rules.annotationtests.basic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface SimpleTestAnnotation {
    String value1();

    String value2();
}
