package org.jboss.windup.config.metadata;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.furnace.FurnaceHolder;
import org.jboss.windup.config.loader.RuleLoaderContext;

/**
 * Provides a mechanism for transforming from one {@link TechnologyReference} to another.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TechnologyReferenceTransformer
{
    private static final String KEY = TechnologyReferenceTransformer.class.getCanonicalName();

    private final TechnologyReference original;
    private final TechnologyReference target;

    /**
     * Creates a transformer that converts references matching the original, to one matching the given destination.
     */
    public TechnologyReferenceTransformer(TechnologyReference original, TechnologyReference target)
    {
        this.original = original;
        this.target = target;
    }

    /**
     * Gets the transformers for the given graph context, or loads them if necessary.
     */
    public static List<TechnologyReferenceTransformer> getTransformers(RuleLoaderContext ruleLoaderContext)
    {
        List<TechnologyReferenceTransformer> transformerList = new ArrayList<>();
        Iterable<TechnologyReferenceTransformerLoader> loaders = FurnaceHolder.getFurnace().getAddonRegistry()
                .getServices(TechnologyReferenceTransformerLoader.class);
        loaders.forEach((loader) -> {
            transformerList.addAll(loader.loadTransformers(ruleLoaderContext));
        });
        return transformerList;
    }

    /**
     * Gets the type to transform from.
     */
    public TechnologyReference getOriginal()
    {
        return original;
    }

    /**
     * Gets the type to transform to.
     */
    public TechnologyReference getTarget()
    {
        return target;
    }

    /**
     * If the given reference matches the original, then this will return the target {@link TechnologyReference}.
     */
    public TechnologyReference transform(TechnologyReference reference)
    {
        if (this.original.matches(reference))
            return target;
        else
            return reference;
    }

    /**
     * If the given reference matches the original, then this will return the target {@link TechnologyReference}.
     */
    public TechnologyReference transform(String idAndVersion)
    {
        TechnologyReference reference = TechnologyReference.parseFromIDAndVersion(idAndVersion);

        if (this.original.matches(reference))
            return target;
        else
            return reference;
    }
}
