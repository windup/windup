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
 *    {tag name="bar" root="true"}
 *    {tag name="baz"}
 *    ...
 * {tag name="bar"}
 *    {tag name="boo"  parents="bar baz"}
 *    ...
 */
public class TagsSaxHandler extends DefaultHandler
{
    private static final Logger log = Logger.getLogger(TagsSaxHandler.class.getName() );

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
            Tag tag = tagService.getOrCreateTag(tagName);

            if ("true".equals(attributes.getValue("root")))
                tag.setIsRoot(true);

            if ("true".equals(attributes.getValue("pseudo")))
                tag.setPseudo(true);

            tag.setTitle(attributes.getValue("title"));

            final String color = attributes.getValue("color");
            if (color != null)
            {
                if (color.matches("#\\p{XDigit}{6}"))
                    tag.setColor(color);
                else
                    log.fine("Invalid color, not matching #\\p{XDigit}{6}: " + color);
            }

            // Add this <tag> to its parent.
            if(!stack.empty())
                stack.peek().addContainedTag(tag);

            stack.push(tag);

            // Add the tags named in containedBy="...".
            String[] containedBy = StringUtils.split( StringUtils.defaultString(attributes.getValue("parents")), " ,");
            for (String containingTagName : containedBy)
            {
                Tag containingTag = tagService.getOrCreateTag(containingTagName);
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
