package org.jboss.windup.reporting.config;

/**
 * Represents a link to an external resource, such as an URL.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Link {
    private final String link;
    private final String title;

    private Link(String link, String title) {
        this.link = link;
        this.title = title;
    }

    /**
     * Create a new {@link Link} instance with the given target and title.
     */
    public static Link to(String description, String link) {
        return new Link(link, description);
    }

    /**
     * Get the {@link Link} value.
     */
    public String getLink() {
        return link;
    }

    /**
     * Get the title of this {@link Link}.
     */
    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Link.to(" + link + ").titled(" + title + ")";
    }
}
