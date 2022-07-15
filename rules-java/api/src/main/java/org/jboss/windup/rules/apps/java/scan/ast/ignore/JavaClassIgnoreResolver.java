package org.jboss.windup.rules.apps.java.scan.ast.ignore;

import org.jboss.windup.rules.apps.java.scan.ast.trie.TriePrefixStructure;
import org.jboss.windup.rules.apps.java.scan.ast.trie.TrieStructureTypeRelation;

/**
 * An instance of a trie structure {@link TriePrefixStructure} that saves <javaclass-ignore>s
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class JavaClassIgnoreResolver extends TriePrefixStructure<String, String> {
    private static JavaClassIgnoreResolver defaultInstance;

    /**
     * Gets the default instance of the {@link JavaClassIgnoreResolver}. This is not thread safe.
     */
    public static JavaClassIgnoreResolver singletonInstance() {
        if (defaultInstance == null) {
            TrieStructureTypeRelation<String, String> relation = new TrieStructureTypeRelation<String, String>() {

                @Override
                public String getStringToSearchFromSearchType(String search) {
                    return search;
                }

                @Override
                public String getStringPrefixToSaveSaveType(String save) {
                    /**
                     * At least for now javaclass-ignore does not contain anything except the prefix
                     */
                    return save;
                }

                @Override
                public boolean checkIfMatchFound(String saved, String searched) {
                    return searched.startsWith(saved);
                }
            };
            defaultInstance = new JavaClassIgnoreResolver(relation);
        }
        return defaultInstance;
    }

    public JavaClassIgnoreResolver(TrieStructureTypeRelation relation) {
        super(relation);
    }


}
