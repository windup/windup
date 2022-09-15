package org.jboss.windup.config.tags;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class TagsLibTest {
    @Test
    public void testTagsLoading() throws Exception {
        final TagService tagService = new TagService();

        File file = new File("src/test/java/org/jboss/windup/config/tags/test2.tags.xml");
        try (InputStream is = new FileInputStream(file)) {
            tagService.readTags(is);
        } catch (IOException ex) {
            throw ex;
        }
        Assert.assertNotNull(tagService.getTag("a-prime"));
        Assert.assertNotNull(tagService.getTag("a1"));
        Assert.assertNull(tagService.findTag("non-existent"));
        Assert.assertNotNull(tagService.getOrCreateTag("a1", true));
        Assert.assertNotNull(tagService.getOrCreateTag("to-be-created", false));

        Assert.assertTrue("a-prime contains a1", tagService.isUnderTag("a-prime", "a1"));
        Assert.assertFalse("a1 not contains a-prime", tagService.isUnderTag("a1", "a-prime"));
        Assert.assertFalse("foo not contains bar", tagService.isUnderTag("foo", "bar"));

        Assert.assertTrue("a-prime contains a1", tagService.isUnderTag("a-prime", "a1"));
        Assert.assertFalse("a1 not contains a-prime", tagService.isUnderTag("a1", "a-prime"));
        Assert.assertFalse("foo not contains bar", tagService.isUnderTag("foo", "bar"));

        Assert.assertTrue(tagService.isUnderTag("a-prime", "a1a"));
        Assert.assertFalse(tagService.isUnderTag("a1a", "a-prime"));

        // parents="..."
        // <tag name="c1" parents="b1a1"/>
        Assert.assertTrue(tagService.isUnderTag("b1a1", "c1"));
        Assert.assertTrue(tagService.isUnderTag("b1a1", "c1a"));
        Assert.assertFalse(tagService.isUnderTag("c1", "b1a1"));

        final List<Tag> primeTags = tagService.getPrimeTags();
        System.out.println(primeTags);
        Assert.assertTrue(primeTags.contains(new Tag("b1-prime")));
        Assert.assertTrue(primeTags.contains(new Tag("b1a-prime")));
        Assert.assertFalse(primeTags.contains(new Tag("b")));

        final List<Tag> rootTags = tagService.getRootTags();
        Assert.assertTrue(rootTags.contains(new Tag("a-prime")));
        Assert.assertTrue(rootTags.contains(new Tag("b")));
        Assert.assertTrue(rootTags.contains(new Tag("c")));
        Assert.assertFalse(rootTags.contains(new Tag("c1")));

        Set<Tag> tags = tagService.getAncestorTags(tagService.getTag("c1a"));
        Assert.assertTrue(tags.contains(new Tag("c1")));
        Assert.assertTrue(tags.contains(new Tag("c")));
        Assert.assertTrue(tags.contains(new Tag("b1a1")));
        Assert.assertTrue(tags.contains(new Tag("b1a-prime")));
        Assert.assertFalse(tags.contains(new Tag("a-prime")));
        Assert.assertFalse(tags.contains(new Tag("non-existent")));
        Assert.assertFalse(tags.contains(new Tag("b1b")));

        tags = tagService.getDescendantTags(tagService.getTag("b1-prime"));
        Assert.assertTrue(tags.contains(new Tag("b1a-prime")));
        Assert.assertTrue(tags.contains(new Tag("b1a1")));
        Assert.assertTrue(tags.contains(new Tag("b1a2")));
        Assert.assertTrue(tags.contains(new Tag("b1b")));
        Assert.assertTrue(tags.contains(new Tag("c1")));
        Assert.assertTrue(tags.contains(new Tag("c1a")));
        Assert.assertFalse(tags.contains(new Tag("b")));
        Assert.assertFalse(tags.contains(new Tag("c")));

        Assert.assertTrue("#cafeba".equals(tagService.getTag("colorCafe").getColor()));
        Assert.assertTrue("czechbeergolden".equals(tagService.getTag("colorBeer").getColor()));
        Assert.assertTrue(tagService.getTag("not a GOOD tag Name").getName().equals("not-a-good-tag-name"));
        Assert.assertTrue(tagService.getTag("some:name").getName().equals("some:name"));
        Assert.assertTrue(tagService.getTag("mars").getTraits().get("someCustomAttribute").contains("Elon"));
    }


    @Test
    public void testTagsExportingToJavaScript() throws Exception {
        final TagService tagService = new TagService();

        File file = new File("src/test/java/org/jboss/windup/config/tags/java-ee.test.tags.xml");
        try (InputStream is = new FileInputStream(file)) {
            tagService.readTags(is);
        } catch (IOException ex) {
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
