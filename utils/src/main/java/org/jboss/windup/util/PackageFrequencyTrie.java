package org.jboss.windup.util;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class PackageFrequencyTrie {
    private PackageFrequencyTrie parent;
    private String nameElement = "";
    private Map<String, PackageFrequencyTrie> entries = new TreeMap<>(new PackageComparator());
    private int classCount = 0;

    public void visit(PackageFrequencyTrieVisitor visitor) {
        visit(visitor, 0);
    }

    private void visit(PackageFrequencyTrieVisitor visitor, int currentDepth) {
        for (Map.Entry<String, PackageFrequencyTrie> mapEntry : entries.entrySet()) {
            PackageFrequencyTrie subtree = mapEntry.getValue();
            subtree.visit(visitor, currentDepth + 1);
        }

        visitor.visit(this, currentDepth);
    }

    public PackageFrequencyTrie() {
    }

    public PackageFrequencyTrie(PackageFrequencyTrie parent, String nameElement) {
        this.parent = parent;
        this.nameElement = nameElement;
    }

    public String getPackageName() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.nameElement);

        PackageFrequencyTrie parent = this.parent;
        while (parent != null) {
            if (StringUtils.isNotBlank(parent.nameElement)) {
                builder.insert(0, ".");
                builder.insert(0, parent.nameElement);
            }

            parent = parent.parent;
        }

        return builder.toString();
    }

    public int getClassCount(boolean recursive) {
        int count = this.classCount;
        if (recursive) {
            for (Map.Entry<String, PackageFrequencyTrie> entry : this.entries.entrySet())
                count += entry.getValue().getClassCount(true);
        }
        return count;
    }

    public PackageFrequencyTrie addClass(String qualifiedName) {
        String packageName = ClassNameUtil.getPackageName(qualifiedName);
        PackageFrequencyTrie subTrie = getSubTrie(packageName);
        subTrie.classCount++;

        return this;
    }

    private PackageFrequencyTrie getSubTrie(String packageName) {
        PackageFrequencyTrie current = this;

        StringTokenizer tokenizer = new StringTokenizer(packageName, ".");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            PackageFrequencyTrie next = current.entries.get(token);
            if (next == null) {
                next = new PackageFrequencyTrie(current, token);
                current.entries.put(token, next);
            }
            current = next;
        }
        return current;
    }
}
