package org.jboss.windup.rules.apps.java.scan.ast;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.collections.map.LRUMap;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;

/**
 * Static store for type interest information. E.g. Which classes to scan and report on.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public final class TypeInterestFactory
{
    private static Logger LOG = Logging.get(TypeInterestFactory.class);

    private static Map<String, Pattern> patterns = new HashMap<>();

    // cache these lookups in an LRU cache, as there are frequent duplicates (and the regex comparisons are much slower than a cache lookup)
    private static LRUMap resultsCache = new LRUMap(8000);
    private static AtomicLong cacheLookupCount = new AtomicLong(0);
    private static AtomicLong cacheHitCount = new AtomicLong(0);

    /**
     * Register a regex pattern to filter interest in certain Java types.
     */
    public static void registerInterest(String regex)
    {
        /*
         * For now, surround with .* to ensure that regexes will match some of the messier references that the type visitor report.
         */
        patterns.put(regex, Pattern.compile(".*" + regex + ".*"));
    }

    /**
     * Get all the compiled patterns to be used as a filter for Java types.
     */
    public static Set<Pattern> getPatterns()
    {
        return Collections.unmodifiableSet(new HashSet<>(patterns.values()));
    }

    private static Boolean checkCacheForMatches(String text)
    {
        Boolean cachedResult = (Boolean) resultsCache.get(text);
        Long lookupCount = cacheLookupCount.incrementAndGet();
        Long hitCount = cacheHitCount.get();
        if (cachedResult != null)
        {
            hitCount = cacheHitCount.incrementAndGet();
        }
        if (lookupCount % 100000L == 0)
        {
            long hitPercentage = Math.round(((double) hitCount / (double) lookupCount) * 100);
            LOG.info("There have been " + lookupCount + " lookups with " + hitCount + " hits, for a hit percentage of: " + hitPercentage);
        }
        return cachedResult;
    }

    public static boolean matchesAny(String text)
    {
        ExecutionStatistics.get().begin("TypeInterestFactory.matchesAny(text)");

        try
        {
            ExecutionStatistics.get().begin("TypeInterestFactory.matchesAny(text).cacheCheck");
            try
            {
                Boolean cachedResult = checkCacheForMatches(text);
                if (cachedResult != null)
                {
                    return cachedResult;
                }
            }
            finally
            {
                ExecutionStatistics.get().end("TypeInterestFactory.matchesAny(text).cacheCheck");
            }

            ExecutionStatistics.get().begin("TypeInterestFactory.matchesAny(text).manualSearch");
            try
            {
                for (Pattern pattern : patterns.values())
                {
                    if (pattern.matcher(text).matches())
                    {
                        resultsCache.put(text, true);
                        return true;
                    }
                }
                resultsCache.put(text, false);
                return false;
            }
            finally
            {
                ExecutionStatistics.get().end("TypeInterestFactory.matchesAny(text).manualSearch");
            }
        }
        finally
        {
            ExecutionStatistics.get().end("TypeInterestFactory.matchesAny(text)");
        }
    }
}
