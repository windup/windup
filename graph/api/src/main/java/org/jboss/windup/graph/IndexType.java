package org.jboss.windup.graph;


/**
 * Describes an index type. (For use only with the {@link Indexed} annotation.)
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public enum IndexType {
    /**
     * The default index type.
     */
    DEFAULT,

    /**
     * A text-based search index.
     */
    SEARCH,

    /**
     * A list-based index.
     */
    LIST
}
