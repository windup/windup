package org.jboss.windup.config.tags;

import java.util.Stack;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Expects the following XML structure:
 *
 * {tag name="foo"}
 *    {tag name="bar" prime="true"}
 *    {tag name="baz"}
 *    ...
 * {tag name="bar"}
 *    {tag name="boo"  parents="bar baz"}
 *    ...
 */
public class TagsSaxHandler extends DefaultHandler
{
    private static final Logger LOG = Logger.getLogger(TagsSaxHandler.class.getName() );

    private final TagService tagService;
    private final Stack<Tag> stack = new Stack<>();


    public TagsSaxHandler(TagService tagsService)
    {
        this.tagService = tagsService;
    }




    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if("tag".equals(qName))
        {
            String tagName = attributes.getValue("name");
            String tagRef  = attributes.getValue("ref");
            boolean isRef = tagRef != null && !"".equals(tagRef);
            if (isRef)
                tagName = tagRef;

            Tag tag = tagService.getOrCreateTag(tagName, tagRef != null);

            // If it is not a reference, it may be defining a tag that was already referenced;
            // so we need to set the values of the placeholder.
            // On the other hand, some users may use <tag name="..."/> instead of <tag ref="..."/>, so let's not unset them.
            if (!isRef) {
                if ("true".equals(attributes.getValue("prime")))
                    tag.setIsPrime(true);

                if ("true".equals(attributes.getValue("pseudo")))
                    tag.setPseudo(true);

                String title = attributes.getValue("title");
                if (title != null) {
                    if (tag.getTitle() != null)
                        LOG.warning("Redefining tag title to '"+title+"', was: " + tag.toString());
                    tag.setTitle(title);
                }

                final String color = attributes.getValue("color");
                if (color != null)
                {
                    if (tag.getColor()!= null)
                        LOG.warning("Redefining tag color to '"+title+"', was: " + tag.getColor());
                    if (color.matches("#\\p{XDigit}{6}"))
                        tag.setColor(color);
                    else
                        LOG.fine("Invalid color, not matching #\\p{XDigit}{6}: " + color);
                }
            }

            // Add this <tag> to its parent.
            if(!stack.empty())
                stack.peek().addContainedTag(tag);
            else
                tag.setIsRoot(true);

            stack.push(tag);

            // Add the tags named in containedBy="...".
            String[] containedBy = StringUtils.split( StringUtils.defaultString(attributes.getValue("parents")), " ,");
            for (String containingTagName : containedBy)
            {
                Tag containingTag = tagService.getOrCreateTag(containingTagName, true);
                tag.addContainingTag(containingTag);
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if("tag".equals(qName))
        {
            this.stack.pop();
        }
    }

}
