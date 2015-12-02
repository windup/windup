package org.jboss.windup.bootstrap.help;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;
import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.ConfigurationOption;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.util.PathUtil;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class Help
{
    private static final String HELP = "help";
    private static final String OPTION = "option";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";

    private List<OptionDescription> options = new ArrayList<>();

    public List<OptionDescription> getOptions()
    {
        return options;
    }

    private void addOption(OptionDescription optionDescription)
    {
        this.options.add(optionDescription);
    }

    private static File getDefaultFile() throws IOException
    {
        Path helpDirectory = PathUtil.getWindupHome().resolve("cache").resolve("help");
        if (!Files.exists(helpDirectory))
            Files.createDirectories(helpDirectory);
        Path helpPath = helpDirectory.resolve("help.xml");
        return helpPath.toFile();
    }

    public static Help load()
    {
        final Help result = new Help();
        try
        {
            Document doc = new SAXReader().read(getDefaultFile());

            Iterator optionElementIterator = doc.getRootElement().elementIterator(OPTION);
            while (optionElementIterator.hasNext())
            {
                Element optionElement = (Element) optionElementIterator.next();

                String name = optionElement.attributeValue(NAME);
                String description = optionElement.element(DESCRIPTION).getText();

                OptionDescription option = new OptionDescription(name, description);
                result.addOption(option);
            }
        }
        catch (DocumentException | IOException e)
        {
            System.err.println("WARNING: Failed to load detailed help information!");
        }
        return result;
    }

    public static void save(Furnace furnace) throws IOException
    {
        Document doc = new DOMDocument(new DOMElement(HELP));
        Iterable<ConfigurationOption> windupOptions = WindupConfiguration.getWindupConfigurationOptions(furnace);
        for (ConfigurationOption option : windupOptions)
        {
            Element optionElement = new DOMElement(OPTION);
            optionElement.addAttribute(NAME, option.getName());

            Element descriptionElement = new DOMElement(DESCRIPTION);
            descriptionElement.setText(option.getDescription());
            optionElement.add(descriptionElement);

            doc.getRootElement().add(optionElement);
        }

        try (FileWriter writer = new FileWriter(getDefaultFile()))
        {
            doc.write(writer);
        }
    }
}
