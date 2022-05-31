package org.jboss.windup.reporting.quickfix;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface QuickfixTransformation {
    String getTransformationID();

    String transform(QuickfixLocationDTO locationDTO);
}
