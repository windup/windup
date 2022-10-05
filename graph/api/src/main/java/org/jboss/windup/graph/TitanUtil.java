package org.jboss.windup.graph;

/**
 * Contains methods that are useful for dealing with some of the peculiarities of the Titan data store and its indexing scheme.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TitanUtil {
    /**
     * This attempts to convert from standard regular expression syntax to a format that is suitable for
     * a Lucene index search (such as those used by titan).
     */
    public static String titanifyRegex(String regex) {
        return regex.replace("\\Q", "\"").replace("\\E", "\"").replace("?:", "");
        //return regex;
    }
}
