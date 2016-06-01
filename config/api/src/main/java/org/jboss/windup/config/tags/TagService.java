package org.jboss.windup.config.tags;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Singleton;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.SAXException;

/**
 * Manages the relations between Windup tags and provides API to query these relations.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@Singleton
public class TagService
{
    private final ConcurrentMap<String, Tag> definedTags = new ConcurrentHashMap<>();

    /**
     * Read the tag structure from the provided stream.
     */
    public void readTags(InputStream tagsXML)
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try
        {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(tagsXML, new TagsSaxHandler(this));
        }
        catch (ParserConfigurationException | SAXException | IOException ex)
        {
            throw new RuntimeException("Failed parsing the tags definition: " + ex.getMessage(), ex);
        }
    }

    /**
     * Returns the {@link Tag} with the provided name.
     */
    public Tag getTag(String tagName)
    {
        return definedTags.get(tagName);
    }

    /**
     * Gets the {@link Tag} with the given name or creates a new {@link Tag} if one does not already exist.
     */
    public Tag getOrCreateTag(String tagName)
    {
        synchronized (this.definedTags)
        {
            if (definedTags.containsKey(tagName))
                return definedTags.get(tagName);
            else
            {
                final Tag tag = new Tag(tagName);
                definedTags.put(tagName, tag);
                return tag;
            }
        }
    }

    /**
     * Convenience method, calls this.isUnderTag(Tag superTag, Tag subTag).
     */
    public boolean isUnderTag(String superTagName, String subTagName)
    {
        if (superTagName == null || subTagName == null)
            return false;

        if (superTagName.equals(subTagName))
            return false;

        final Tag superTag = this.getTag(superTagName);
        final Tag subTag = this.getTag(subTagName);
        return this.isUnderTag(superTag, subTag);
    }

    /**
     * @return true if the subTag is contained directly or indirectly in the superTag.
     */
    public boolean isUnderTag(Tag superTag, Tag subTag)
    {
        if (superTag == null || subTag == null)
            return false;

        if (superTag.getName().equals(subTag.getName()))
            return false;

        Set<Tag> walkedSet = new LinkedHashSet<>();

        Set<Tag> currentSet = new LinkedHashSet<>();
        currentSet.add(subTag);

        do
        {
            walkedSet.addAll(currentSet);

            Set<Tag> nextSet = new LinkedHashSet<>();
            for (Tag currentTag : currentSet)
            {
                Set<Tag> parentTags = currentTag.getParentTags();
                if (parentTags.contains(superTag))
                    return true;

                nextSet.addAll(currentTag.getParentTags());
            }

            // Prevent infinite loops - detect graph cycles.
            Iterator<Tag> it = walkedSet.iterator();
            while (it.hasNext())
            {
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
    public void writeTagsToJavaScript(Writer writer) throws IOException
    {
        writer.append("function fillTagService(tagService) {\n");
        writer.append("\t// (name, isRoot, isPseudo, color), [parent tags]\n");
        for (Tag tag : definedTags.values())
        {
            writer.append("\ttagService.registerTag(new Tag(");
            escapeOrNull(tag.getName(), writer);
            writer.append(", ");
            escapeOrNull(tag.getTitle(), writer);
            writer.append(", ").append("" + tag.isRoot())
                        .append(", ").append("" + tag.isPseudo())
                        .append(", ");
            escapeOrNull(tag.getColor(), writer);
            writer.append(")").append(", [");

            // We only have strings, not references, so we're letting registerTag() getOrCreate() the tag.
            for (Tag parentTag : tag.getParentTags())
            {
                writer.append("'").append(StringEscapeUtils.escapeEcmaScript(parentTag.getName())).append("',");
            }
            writer.append("]);\n");
        }
        writer.append("}\n");
    }

    private void escapeOrNull(final String string, Writer writer) throws IOException
    {
        if (string == null)
            writer.append("null");
        else
            writer.append('"').append(StringEscapeUtils.escapeEcmaScript(string)).append('"');
    }

    @Override
    public String toString()
    {
        return "TagService{ definedTags: " + definedTags.size() + '}';
    }

}
