package org.jboss.windup.rules.apps.javaee.model;

public enum EnvironmentReferenceTagType {
    RESOURCE_ENV_REF("resource-env-ref"), RESOURCE_REF("resource-ref"), EJB_REF("ejb-ref"), EJB_LOCAL_REF("ejb-local-ref"), MSG_DESTINATION_REF(
            "message-destination-ref");

    private final String tag;

    private EnvironmentReferenceTagType(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public String toString() {
        return name();
    }
}
