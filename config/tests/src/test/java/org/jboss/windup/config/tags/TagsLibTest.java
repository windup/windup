package org.jboss.windup.config.tags;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import org.junit.Assert;
import org.junit.Test;

public class TagsLibTest
{
    @Test
    public void testTagsLoading() throws Exception
    {
        final TagService tagService = new TagService();

        File file = new File("src/test/java/org/jboss/windup/config/tags/test2.tags.xml");
        try(InputStream is = new FileInputStream(file))
        {
            tagService.readTags(is);
        }
        catch( IOException ex )
        {
            throw ex;
        }
        Assert.assertNotNull(tagService.getTag("a-root"));
        Assert.assertNotNull(tagService.getTag("a1"));
        Assert.assertNull(tagService.getTag("non-existent"));
        Assert.assertNotNull(tagService.getOrCreateTag("to-be-created"));

        Assert.assertTrue("a-root contains a1", tagService.isUnderTag("a-root", "a1"));
        Assert.assertFalse("a1 not contains a-root", tagService.isUnderTag("a1", "a-root"));
        Assert.assertFalse("foo not contains bar", tagService.isUnderTag("foo", "bar"));

        Assert.assertTrue("a-root contains a1", tagService.isUnderTag("a-root", "a1"));
        Assert.assertFalse("a1 not contains a-root", tagService.isUnderTag("a1", "a-root"));
        Assert.assertFalse("foo not contains bar", tagService.isUnderTag("foo", "bar"));

        Assert.assertTrue(tagService.isUnderTag("a-root", "a1a"));
        Assert.assertFalse(tagService.isUnderTag("a1a", "a-root"));

        // parents="..."
        // <tag name="c1" parents="b1a1"/>
        Assert.assertTrue(tagService.isUnderTag("b1a1", "c1"));
        Assert.assertTrue(tagService.isUnderTag("b1a1", "c1a"));
        Assert.assertFalse(tagService.isUnderTag("c1", "b1a1"));
    }


    @Test
    public void testTagsExportingToJavaScript() throws Exception
    {
        final TagService tagService = new TagService();

        File file = new File("src/test/java/org/jboss/windup/config/tags/java-ee.test.tags.xml");
        try(InputStream is = new FileInputStream(file))
        {
            tagService.readTags(is);
        }
        catch( IOException ex )
        {
            throw ex;
        }

        StringWriter writer = new StringWriter((int) file.length());
        tagService.writeTagsToJavaScript(writer);
        final String javascript = writer.toString();
        System.out.println(javascript);
        Assert.assertTrue(javascript.contains("function"));
        Assert.assertTrue(javascript.contains("java-ee"));
        Assert.assertTrue(javascript.contains("weblogic"));
    }

}
