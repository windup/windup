package org.jboss.windup.tests.bootstrap;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.jboss.windup.bootstrap.Bootstrap;
import org.jboss.windup.util.PathUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public abstract class AbstractBootstrapTestWithRules extends AbstractBootstrapTest {
    private static final String TESTING_MIGRATION_RULES = "<?xml version=\"1.0\"?>\n"
            + "<ruleset xmlns=\"http://windup.jboss.org/schema/jboss-ruleset\" id=\"BootstrapTests_Eap6to7\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:schemaLocation=\"http://windup.jboss.org/schema/jboss-ruleset "
            + "http://windup.jboss.org/schema/jboss-ruleset/windup-jboss-ruleset.xsd \">\n"
            + "    <metadata>\n"
            + "        <description>Only for bootstrap tests.</description>\n"
            + "        <dependencies>\n"
            + "            <addon id=\"org.jboss.windup.rules,windup-rules-xml," + Bootstrap.getRuntimeAPIVersion() + "\" />\n"
            + "        </dependencies>\n"
            + "        <sourceTechnology id=\"eap6\" versionRange=\"[6,7)\" />\n"
            + "        <targetTechnology id=\"eap7\" versionRange=\"[7,)\" />\n"
            + "        <tag>test-tag-eap</tag>\n"
            + "    </metadata>\n"
            + System.lineSeparator()
            + "    <rules>\n"
            + "        <rule id=\"testing-rule\">\n"
            + "            <when>\n"
            + "                <file filename=\"jboss-web.xml\"/>\n"
            + "            </when>\n"
            + "            <perform>\n"
            + "                <classification title=\"jboss-web.xml\" effort=\"3\" severity=\"mandatory\"/>\n"
            + "            </perform>\n"
            + "        </rule>\n"
            + "    </rules>\n"
            + "</ruleset>";

    // has a different tag, which is needed for some tests
    private static final String MORE_TESTING_MIGRATION_RULES = "<?xml version=\"1.0\"?>\n"
            + "<ruleset xmlns=\"http://windup.jboss.org/schema/jboss-ruleset\" id=\"BootstrapTests_More_Eap6to7\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:schemaLocation=\"http://windup.jboss.org/schema/jboss-ruleset "
            + "http://windup.jboss.org/schema/jboss-ruleset/windup-jboss-ruleset.xsd \">\n"
            + "    <metadata>\n"
            + "        <description>Only for bootstrap tests.</description>\n"
            + "        <dependencies>\n"
            + "            <addon id=\"org.jboss.windup.rules,windup-rules-xml," + Bootstrap.getRuntimeAPIVersion() + "\" />\n"
            + "        </dependencies>\n"
            + "        <sourceTechnology id=\"eap6\" versionRange=\"[6,7)\" />\n"
            + "        <targetTechnology id=\"eap7\" versionRange=\"[7,)\" />\n"
            + "        <tag>another-test-tag-eap</tag>\n"
            + "    </metadata>\n"
            + System.lineSeparator()
            + "    <rules>\n"
            + "        <rule id=\"another-testing-rule\">\n"
            + "            <when>\n"
            + "                <file filename=\"jboss-ejb3.xml\"/>\n"
            + "            </when>\n"
            + "            <perform>\n"
            + "                <classification title=\"jboss-ejb3.xml\" effort=\"3\" severity=\"mandatory\"/>\n"
            + "            </perform>\n"
            + "        </rule>\n"
            + "    </rules>\n"
            + "</ruleset>";

    @Rule
    public final TemporaryFolder rulesDir = new TemporaryFolder();

    @Before
    public void setUpRulesDirectory() throws IOException {
        File rules = rulesDir.newFile("test-eap6to7-rules.windup.xml");
        Files.write(TESTING_MIGRATION_RULES, rules, Charsets.UTF_8);

        File moreRules = rulesDir.newFile("test-eap6to7-more-rules.windup.xml");
        Files.write(MORE_TESTING_MIGRATION_RULES, moreRules, Charsets.UTF_8);

        System.setProperty(PathUtil.WINDUP_RULESETS_DIR_SYSPROP, rulesDir.getRoot().getAbsolutePath());
    }

    @After
    public void cleanSystemProperty() {
        System.clearProperty(PathUtil.WINDUP_RULESETS_DIR_SYSPROP);
    }
}
