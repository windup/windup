package org.jboss.windup.util;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface PackageFrequencyTrieVisitor {
    void visit(PackageFrequencyTrie trie, int depth);
}
