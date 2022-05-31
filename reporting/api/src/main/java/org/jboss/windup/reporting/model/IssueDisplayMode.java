package org.jboss.windup.reporting.model;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public enum IssueDisplayMode {
    DETAIL_ONLY("detail-only"), SUMMARY_ONLY("summary-only"), ALL("all");

    private String name;

    IssueDisplayMode(String name) {
        this.name = name;
    }

    public static IssueDisplayMode parse(String value) {
        for (IssueDisplayMode issueDisplayMode : IssueDisplayMode.values()) {
            if (StringUtils.equalsIgnoreCase(value, issueDisplayMode.name()))
                return issueDisplayMode;
            else if (StringUtils.equalsIgnoreCase(value, issueDisplayMode.toString()))
                return issueDisplayMode;
        }
        throw new IllegalArgumentException("No IssueDisplay mode available for: " + value);
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static class Defaults {
        public static final IssueDisplayMode DEFAULT_DISPLAY_MODE = IssueDisplayMode.ALL;
    }
}
