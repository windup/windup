package org.jboss.windup.config.tags;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jboss.windup.util.exception.WindupException;
import org.xml.sax.SAXException;

/**
 * Manages the relations between Windup tags and provides API to query these relations.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@Singleton
public class TagService {
    private final ConcurrentMap<String, Tag> definedTags = new ConcurrentHashMap<>();

    /**
     * Read the tag structure from the provided stream.
     */
    public void readTags(InputStream tagsXML) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(tagsXML, new TagsSaxHandler(this));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new RuntimeException("Failed parsing the tags definition: " + ex.getMessage(), ex);
        }
    }

    /**
     * Gets all tags that are "prime" tags.
     */
    public List<Tag> getPrimeTags() {
        return this.definedTags.values().stream()
                .filter(Tag::isPrime)
                .collect(Collectors.toList());
    }

    /**
     * Returns the tags that were root in the definition files. These serve as entry point shortcuts when browsing the graph. We could reduce this to
     * just fewer as the root tags may be connected through parents="...".
     */
    public List<Tag> getRootTags() {
        return this.definedTags.values().stream()
                .filter(Tag::isRoot)
                .collect(Collectors.toList());
    }

    /**
     * Returns the {@link Tag} with the provided name.
     */
    public Tag findTag(String tagName) {
        if (null == tagName)
            throw new IllegalArgumentException("Looking for null tag name.");
        return definedTags.get(Tag.normalizeName(tagName));
    }

    public Tag getTag(String tagName) {
        Tag tag = findTag(tagName);
        if (null == tag)
            throw new WindupException("Tag does not exist: " + tagName);
        return tag;
    }

    /**
     * Gets the {@link Tag} with the given name or creates a new {@link Tag} if one does not already exist.
     *
     * @param isRef True if the given tag name is a reference, in which case it should already exist.
     */
    public Tag getOrCreateTag(String tagName, boolean isRef) {
        if (null == tagName)
            throw new IllegalArgumentException("Looking for a null tag name.");
        tagName = Tag.normalizeName(tagName);

        synchronized (this.definedTags) {
            if (definedTags.containsKey(tagName))
                return definedTags.get(tagName);
            else {
                final Tag tag = new Tag(tagName);
                definedTags.put(tagName, tag);
                return tag;
            }
        }
    }

    /**
     * Returns all tags that designate this tag. E.g., for "tesla-model3", this would return "car", "vehicle", "vendor-tesla" etc.
     */
    public Set<Tag> getAncestorTags(Tag tag) {
        Set<Tag> ancestors = new HashSet<>();
        getAncestorTags(tag, ancestors);
        return ancestors;
    }

    private void getAncestorTags(Tag tag, Set<Tag> putResultsHere) {
        for (Tag parentTag : tag.getParentTags()) {
            if (!putResultsHere.add(parentTag))
                continue; // Already visited.
            getAncestorTags(parentTag, putResultsHere);
        }
    }

    /**
     * Returns all tags that are designated by this tag. E.g., for "vehicle", this would return "ship", "car", "tesla-model3", "bike", etc.
     */
    public Set<Tag> getDescendantTags(Tag tag) {
        Set<Tag> ancestors = new HashSet<>();
        getDescendantTags(tag, ancestors);
        return ancestors;
    }

    private void getDescendantTags(Tag tag, Set<Tag> putResultsHere) {
        for (Tag childTag : tag.getContainedTags()) {
            if (!putResultsHere.add(childTag))
                continue; // Already visited.
            getDescendantTags(childTag, putResultsHere);
        }
    }

    /**
     * Convenience method, calls this.isUnderTag(Tag superTag, Tag subTag).
     */
    public boolean isUnderTag(String superTagName, String subTagName) {
        if (superTagName == null || subTagName == null)
            throw new IllegalArgumentException("Looking for a null tag name.");

        superTagName = Tag.normalizeName(superTagName);
        subTagName = Tag.normalizeName(subTagName);

        if (superTagName.equals(subTagName))
            return false;

        final Tag superTag = this.findTag(superTagName);
        final Tag subTag = this.findTag(subTagName);
        if (subTag == null || superTag == null)
            return false;
        return this.isUnderTag(superTag, subTag);
    }

    /**
     * @return true if the subTag is contained directly or indirectly in the superTag.
     */
    public boolean isUnderTag(Tag superTag, Tag subTag) {
        if (superTag == null || subTag == null)
            throw new IllegalArgumentException("Looking for a null tag name.");

        if (superTag.getName().equals(subTag.getName()))
            return false;

        Set<Tag> walkedSet = new LinkedHashSet<>();

        Set<Tag> currentSet = new LinkedHashSet<>();
        currentSet.add(subTag);

        do {
            walkedSet.addAll(currentSet);

            Set<Tag> nextSet = new LinkedHashSet<>();
            for (Tag currentTag : currentSet) {
                Set<Tag> parentTags = currentTag.getParentTags();
                if (parentTags.contains(superTag))
                    return true;

                nextSet.addAll(currentTag.getParentTags());
            }

            // Prevent infinite loops - detect graph cycles.
            Iterator<Tag> it = walkedSet.iterator();
            while (it.hasNext()) {
                Tag walkedTag = it.next();
                if (nextSet.contains(walkedTag))
                    nextSet.remove(walkedTag);
            }

            currentSet = nextSet;
        }
        while (!currentSet.isEmpty());
        return false;
    }

    /**
     * Writes the JavaScript code describing the tags as Tag classes to given writer.
     */
    public void writeTagsToJavaScript(Writer writer) throws IOException {
        writer.append("function fillTagService(tagService) {\n");
        writer.append("\t// (name, isPrime, isPseudo, color), [parent tags]\n");
        for (Tag tag : definedTags.values()) {
            writer.append("\ttagService.registerTag(new Tag(");
            escapeOrNull(tag.getName(), writer);
            writer.append(", ");
            escapeOrNull(tag.getTitle(), writer);
            writer.append(", ").append("" + tag.isPrime())
                    .append(", ").append("" + tag.isPseudo())
                    .append(", ");
            escapeOrNull(tag.getColor(), writer);
            writer.append(")").append(", [");

            // We only have strings, not references, so we're letting registerTag() getOrCreate() the tag.
            for (Tag parentTag : tag.getParentTags()) {
                writer.append("'").append(StringEscapeUtils.escapeEcmaScript(parentTag.getName())).append("',");
            }
            writer.append("]);\n");
        }
        writer.append("}\n");
    }

    private void escapeOrNull(final String string, Writer writer) throws IOException {
        if (string == null)
            writer.append("null");
        else
            writer.append('"').append(StringEscapeUtils.escapeEcmaScript(string)).append('"');
    }

    @Override
    public String toString() {
        return "TagService{ definedTags: " + definedTags.size() + '}';
    }

}
