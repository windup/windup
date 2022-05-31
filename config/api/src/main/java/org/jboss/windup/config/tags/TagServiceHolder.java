package org.jboss.windup.config.tags;


import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.furnace.FileExtensionFilter;
import org.jboss.windup.util.furnace.FurnaceClasspathScanner;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Loads the tags relations from tags definition files
 * and provides API to query these relations.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@Singleton
public class TagServiceHolder {
    private static final Logger log = Logger.getLogger(TagServiceHolder.class.getName());


    private TagService tagService = new TagService();


    @Inject
    private Furnace furnace;

    @Inject
    private FurnaceClasspathScanner scanner;


    /**
     * Loads the tag definitions from the files with a ".tags.xml" suffix from the addons.
     */
    @PostConstruct
    public void loadTagDefinitions() {
        Map<Addon, List<URL>> addonToResourcesMap = scanner.scanForAddonMap(new FileExtensionFilter("tags.xml"));
        for (Map.Entry<Addon, List<URL>> entry : addonToResourcesMap.entrySet()) {
            for (URL resource : entry.getValue()) {
                log.info("Reading tags definitions from: " + resource.toString() + " from addon " + entry.getKey().getId());
                try (InputStream is = resource.openStream()) {
                    tagService.readTags(is);
                } catch (Exception ex) {
                    throw new WindupException("Failed reading tags definition: " + resource.toString() + " from addon " + entry.getKey().getId() + ":\n" + ex.getMessage(), ex);
                }
            }
        }
    }


    public TagService getTagService() {
        return tagService;
    }
}
