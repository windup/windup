package org.jboss.windup.scan.ast;

import org.jboss.windup.rules.apps.java.scan.ast.trie.TriePrefixStructure;
import org.jboss.windup.rules.apps.java.scan.ast.trie.TrieStructureTypeRelation;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mbriskar on 9/28/15.
 */
public class TriePrefixStructureTest {
    private static int firstTestMatches = 0;
    private static int secondTestMatches = 0;

    @Test
    public void trieWithStringsTest() {

        TrieStructureTypeRelation<String, String> relation = new TrieStructureTypeRelation<String, String>() {

            @Override
            public String getStringToSearchFromSearchType(String search) {
                return search;
            }

            @Override
            public String getStringPrefixToSaveSaveType(String save) {
                return save;
            }

            @Override
            public boolean checkIfMatchFound(String saved, String searched) {
                firstTestMatches++;
                return saved.equals(searched);
            }
        };
        TriePrefixStructure<String, String> trie = new TriePrefixStructure<>(relation);
        trie.addInterest("abc.def.ghi");
        trie.addInterest("abc");
        trie.addInterest("xyz");
        Assert.assertTrue(trie.matches("abc"));
        Assert.assertEquals(1, firstTestMatches);
        firstTestMatches = 0;
        Assert.assertTrue(trie.matches("xyz"));
        Assert.assertEquals(1, firstTestMatches);
        firstTestMatches = 0;
        Assert.assertFalse(trie.matches("abc.def.ghi.ijkl"));
        Assert.assertEquals(2, firstTestMatches);
    }

    @Test
    public void trieWithObjectsTest() {
        TrieStructureTypeRelation<FirstThreePrefix, FirstThreePrefix> relation = new TrieStructureTypeRelation<FirstThreePrefix, FirstThreePrefix>() {
            @Override
            public String getStringToSearchFromSearchType(FirstThreePrefix search) {
                return search.text.substring(0, 2);
            }

            @Override
            public String getStringPrefixToSaveSaveType(FirstThreePrefix save) {
                return save.text.substring(0, 2);
            }

            @Override
            public boolean checkIfMatchFound(FirstThreePrefix saved, FirstThreePrefix searched) {
                secondTestMatches++;
                return saved.equals(searched);
            }
        };
        TriePrefixStructure<FirstThreePrefix, FirstThreePrefix> trie = new TriePrefixStructure<>(relation);
        trie.addInterest(new FirstThreePrefix("abc.def.ghi"));
        trie.addInterest(new FirstThreePrefix("abc"));
        trie.addInterest(new FirstThreePrefix("xyz"));
        Assert.assertFalse(trie.matches(new FirstThreePrefix("abc.def")));
        Assert.assertEquals(2, secondTestMatches);
        secondTestMatches = 0;
        Assert.assertTrue(trie.matches(new FirstThreePrefix("xyz")));
        Assert.assertEquals(1, secondTestMatches);
        secondTestMatches = 0;
        Assert.assertFalse(trie.matches(new FirstThreePrefix("abc.def.ghi.ijkl")));
        Assert.assertEquals(2, secondTestMatches);
    }

    private class FirstThreePrefix {
        public String text;

        public FirstThreePrefix(String text) {
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            FirstThreePrefix that = (FirstThreePrefix) o;
            return text.equals(that.text);

        }

        @Override
        public int hashCode() {
            return text.hashCode();
        }
    }
}
