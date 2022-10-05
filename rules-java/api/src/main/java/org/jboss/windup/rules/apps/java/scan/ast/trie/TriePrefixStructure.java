package org.jboss.windup.rules.apps.java.scan.ast.trie;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * An abstract prefix trie implementation saving the {@link SAVE_TYPE} in multiple levels of a trie structure based on transforming this {@link SAVE_TYPE} into
 * String. Then this structure is used to query the {@link SEARCH_TYPE} based on it's transformation to String. The query is handled not only on the final
 * node, but on the all nodes visited during prefix search.
 * <p>
 * Real life example: Save regex "abc{*}a" in the node with prefix "abc" by providing transforming method from "abc{*}a" to "abc" accordingly.
 * Then for the {@link SEARCH_TYPE} transformed to a prefix String "abca", all the regexes saved in "","a","ab","abc","abca" will be checked, but not any other.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TriePrefixStructure<SAVE_TYPE, SEARCH_TYPE> {
    private static TriePrefixStructure defaultInstance;

    private final Set<SAVE_TYPE> currentLevelSet = new LinkedHashSet<>();
    private final Map<Character, TriePrefixStructure<SAVE_TYPE, SEARCH_TYPE>> typeInterestMap = new HashMap<>(26);

    private final TrieStructureTypeRelation relation;

    public TriePrefixStructure(TrieStructureTypeRelation<SAVE_TYPE, SEARCH_TYPE> relation) {
        this.relation = relation;
    }

    public void clear() {
        this.currentLevelSet.clear();
        this.typeInterestMap.clear();
    }


    public boolean matches(SEARCH_TYPE search) {
        // package should always be at least an empty string
        String prefix = relation.getStringToSearchFromSearchType(search);
        return matches(prefix, search, 0);
    }

    private boolean matches(String prefix, SEARCH_TYPE search, int currentIndex) {
        /*
         Since the packageName of TypeInterest that was registered may not have been in the full form,
         we need to check also all the sets that are on our path through the trie.
          */
        if (!currentLevelSet.isEmpty()) {
            for (SAVE_TYPE interest : currentLevelSet) {

                if (relation.checkIfMatchFound(interest, search)) {
                    return true;
                }

            }
        }

        if (currentIndex >= prefix.length()) {
            return false;
        }

        Character currentCharacter = prefix.charAt(currentIndex);
        TriePrefixStructure<SAVE_TYPE, SEARCH_TYPE> resolver = typeInterestMap.get(currentCharacter);
        if (resolver == null)
            return false;

        return resolver.matches(prefix, search, currentIndex + 1);
    }

    public void addInterest(SAVE_TYPE save) {
        String prefix = relation.getStringPrefixToSaveSaveType(save);
        addInterest(prefix, save, 0);
    }

    private void addInterest(String referencePrefix, SAVE_TYPE save, int currentIndex) {
        if (currentIndex >= referencePrefix.length()) {
            currentLevelSet.add(save);
            return;
        }

        Character currentCharacter = referencePrefix.charAt(currentIndex);

        TriePrefixStructure resolver = typeInterestMap.get(currentCharacter);
        if (resolver == null) {
            resolver = new TriePrefixStructure<>(relation);
            typeInterestMap.put(currentCharacter, resolver);
        }

        resolver.addInterest(referencePrefix, save, currentIndex + 1);
    }
}
