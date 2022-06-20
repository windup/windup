package org.jboss.windup.rules.apps.java.scan.ast.trie;

/**
 * An interface definining relations between the types used to be saved in a Trie structure. These methods are extracted in order for the {@link TriePrefixStructure}
 * to not be abstract (so it may instantiate it's own implementations).
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public interface TrieStructureTypeRelation<SAVE_TYPE, SEARCH_TYPE> {
    String getStringToSearchFromSearchType(SEARCH_TYPE search);

    String getStringPrefixToSaveSaveType(SAVE_TYPE save);

    boolean checkIfMatchFound(SAVE_TYPE saved, SEARCH_TYPE searched);
}
