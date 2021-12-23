package org.jboss.windup.config.metadata;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.furnace.FurnaceHolder;
import org.jboss.windup.config.loader.RuleLoaderContext;

/**
 * Translates from one {@link TechnologyReference} to another, ie. from "eap7" to "eap:7".
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TechnologyReferenceAliasTranslator
{
    private final TechnologyReference original;
    private final TechnologyReference target;

    /**
     * Creates a translator that converts references matching the name, to one matching the given target.
     */
    public TechnologyReferenceAliasTranslator(TechnologyReference original, TechnologyReference target)
    {
        this.original = original;
        this.target = target;
    }

    /**
     * Gets the translators for the given graph context, or loads them if necessary.
     */
    public static List<TechnologyReferenceAliasTranslator> getTranslators(RuleLoaderContext ruleLoaderContext)
    {
        List<TechnologyReferenceAliasTranslator> transformerList = new ArrayList<>();
        Iterable<TechnologyReferenceAliasTranslatorLoader> loaders = FurnaceHolder.getFurnace().getAddonRegistry()
                .getServices(TechnologyReferenceAliasTranslatorLoader.class);
        loaders.forEach((loader) -> {
            transformerList.addAll(loader.loadTranslators(ruleLoaderContext));
        });
        return transformerList;
    }

    /**
     * Gets the type to translate from.
     */
    public TechnologyReference getOriginalTechnology()
    {
        return original;
    }

    /**
     * Gets the type to translate to.
     */
    public TechnologyReference getTargetTechnology()
    {
        return target;
    }

    /**
     * If the given reference matches the source technology, then this will return the target technology {@link TechnologyReference}.
     */
    public TechnologyReference translate(TechnologyReference technology)
    {
        return original.matches(technology) ? target : technology;
    }

    /**
     * If the given reference matches the source technology, then this will return the target technology {@link TechnologyReference}.
     */
    public TechnologyReference translate(String technologyIdAndVersion)
    {
        TechnologyReference technology = TechnologyReference.parseFromIDAndVersion(technologyIdAndVersion);

        return original.matches(technology) ? target : technology;
    }
}
