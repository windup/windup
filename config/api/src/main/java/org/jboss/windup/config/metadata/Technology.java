package org.jboss.windup.config.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A descriptor for {@link RuleMetadata#sourceTechnology()} and {@link RuleMetadata#targetTechnology()}
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Technology {
    /**
     * Returns a unique identifier for the technology being described.
     */
    String id() default "";

    /**
     * Returns a compatible version-range for the technology being described.
     */
    String versionRange() default "";
}