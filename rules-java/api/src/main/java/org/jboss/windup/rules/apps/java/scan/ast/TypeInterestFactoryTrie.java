package org.jboss.windup.rules.apps.java.scan.ast;

import org.jboss.windup.rules.apps.java.scan.ast.trie.TriePrefixStructure;
import org.jboss.windup.rules.apps.java.scan.ast.trie.TrieStructureTypeRelation;

/**
 * A trie structure that distributes regexes based on the prefix that is achievable. Then when the type interest is searched, it will search based on
 * it's prefix up to the point where no regexes are found.
 * <p>
 * Example: Save regex "abc{*}a" in the node with prefix "abc" by providing transforming method from "abc{*}a" to "abc" accordingly. Then for the
 * String "abca", all the regexes saved in "","a","ab","abc","abca" will be checked, but not any other.
 */
public class TypeInterestFactoryTrie extends TriePrefixStructure<RewritePatternToRegex, String> {
    /**
     * Gets the default instance of the {@link TypeInterestFactoryTrie}.
     */
    public static TypeInterestFactoryTrie newDefaultInstance() {
        TrieStructureTypeRelation<RewritePatternToRegex, String> relation = new TrieStructureTypeRelation<RewritePatternToRegex, String>() {

            @Override
            public String getStringToSearchFromSearchType(String search) {
                return search;
            }

            @Override
            public String getStringPrefixToSaveSaveType(RewritePatternToRegex save) {
                /**
                 * Take the prefix of the windup regex and save it in the node up to the first "{..}"
                 */
                return save.getRewritePattern().split("\\{")[0];
            }

            @Override
            public boolean checkIfMatchFound(RewritePatternToRegex saved, String searched) {
                return saved.getCompiledRegex().matcher(searched).matches();
            }
        };
        return new TypeInterestFactoryTrie(relation);
    }

    private TypeInterestFactoryTrie(TrieStructureTypeRelation relation) {
        super(relation);
    }

}