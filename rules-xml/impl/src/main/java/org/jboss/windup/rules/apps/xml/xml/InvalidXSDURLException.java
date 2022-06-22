package org.jboss.windup.rules.apps.xml.xml;

import java.net.MalformedURLException;

/**
 * A Malformed URL exception that also contains the original invalid URL.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
class InvalidXSDURLException extends MalformedURLException {
    private final String url;

    public InvalidXSDURLException(String msg, String url) {
        super(msg);
        this.url = url;
    }

    /**
     * Gets the problematic URL.
     */
    public String getUrl() {
        return url;
    }
}
