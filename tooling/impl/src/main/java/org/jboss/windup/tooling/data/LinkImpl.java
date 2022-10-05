package org.jboss.windup.tooling.data;

/**
 * Contains information about a Link (href and name).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class LinkImpl implements Link {
    private static final long serialVersionUID = 1L;

    private String description;
    private String url;

    /**
     * Contains a description of the link.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Contains a description of the link.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Contains the URL.
     */
    @Override
    public String getUrl() {
        return url;
    }

    /**
     * Contains the URL.
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
