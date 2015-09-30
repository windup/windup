package org.jboss.windup.rules.apps.java.scan.ast;

import org.jboss.windup.rules.apps.java.scan.ast.trie.TriePrefixStructure;
import org.jboss.windup.rules.apps.java.scan.ast.trie.TrieStructureTypeRelation;

/**
 * A trie structure that distributes regexes based on the prefix that is achievable. Then when the type interest is searched, it will search based
 * on it's prefix up to the point where no regexes are found.
 *
 * Example: Save regex "abc{*}a" in the node with prefix "abc" by providing transforming method from "abc{*}a" to "abc" accordingly.
 * Then for the String "abca", all the regexes saved in "","a","ab","abc","abca" will be checked, but not any other.
 */
public class TypeInterestFactoryTrie extends TriePrefixStructure<WindupRegexToRegex, String>
{
    private static TypeInterestFactoryTrie defaultInstance;

    /**
     * Gets the default instance of the {@link TypeInterestFactoryTrie}. This is not thread safe.
     */
    public static TypeInterestFactoryTrie newDefaultInstance()
    {
        TrieStructureTypeRelation<WindupRegexToRegex, String> relation = new TrieStructureTypeRelation<WindupRegexToRegex, String>()
        {

            @Override public String getStringToSearchFromSearchType(String search)
            {
                return search;
            }

            @Override public String getStringPrefixToSaveSaveType(WindupRegexToRegex save)
            {
                /**
                 * Take the prefix of the windup regex and save it in the node up to the first "{..}"
                 */
                return save.getWindupRegex().split("\\{")[0];
            }

            @Override public boolean checkIfMatchFound(WindupRegexToRegex saved, String searched)
            {
                return saved.getCompiledRegex().matcher(searched).matches();
            }
        };
        return new TypeInterestFactoryTrie(relation);
    }




    private TypeInterestFactoryTrie(TrieStructureTypeRelation relation)
    {
        super(relation);
    }

}