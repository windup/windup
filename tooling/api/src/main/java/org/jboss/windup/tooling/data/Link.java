package org.jboss.windup.tooling.data;

import java.io.Serializable;

/**
 * Contains information about a Link (href and name).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface Link extends Serializable {
    /**
     * Contains a description of the link.
     */
    public String getDescription();

    /**
     * Contains the URL.
     */
    public String getUrl();
}
