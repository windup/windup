package org.jboss.windup.rules.apps.java.reporting.freemarker.dto;

/**
 * A data transfer object carying information about the dependencies' relations
 *
 * @author <a href="mailto:marcorizzi82@gmail.com>Marco Rizzi</a>
 */
public class DependencyGraphRelation {
    private final String source;
    private final String target;

    public DependencyGraphRelation(final String source, final String target) {
        this.source = source;
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }
}
