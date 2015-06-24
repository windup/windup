package org.jboss.windup.config.tags;


import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import javax.inject.Singleton;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;


/**
 * Manages the relations between Windup tags
 * and provides API to query these relations.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@Singleton
public class TagService
{
    private static final Logger log = Logger.getLogger( TagService.class.getName() );


    private ConcurrentMap<String, Tag> definedTags = new ConcurrentHashMap<>();


    public void readTags(InputStream tagsXML)
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(tagsXML, new TagsSaxHandler(this));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new RuntimeException("Failed parsing the tags definition: " + ex.getMessage(), ex);
        }
    }


    public Tag getTag(String tagName)
    {
       return definedTags.get(tagName);
    }

    public Tag getOrCreateTag(String tagName)
    {
        synchronized(this.definedTags){
            if(definedTags.containsKey(tagName))
                return definedTags.get(tagName);
            else {
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


        do {
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
            while(it.hasNext())
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

}
