package org.jboss.windup.rules.apps.java.scan.ast;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    // Keep track of each pattern, as well as an identifier of who gave the pattern to us (so that we can update it)
    private static Map<String, PatternAndLocation> patternsBySource = new HashMap<>();

    // The full list of patterns, organized by location (including null for the case of no location specified)
    private static Map<TypeReferenceLocation, Map<String, Pattern>> patternsByLocation = new HashMap<>();

    private static Set<String> ignorePatternSet = Collections.synchronizedSet(new HashSet<String>());
    static
    {
        ignorePatternSet.add("void");
        ignorePatternSet.add("String");
        ignorePatternSet.add("java.lang.String");
        ignorePatternSet.add("boolean");
        ignorePatternSet.add("Boolean");
        ignorePatternSet.add("java.lang.Boolean");
        ignorePatternSet.add("int");
        ignorePatternSet.add("Integer");
        ignorePatternSet.add("java.lang.Integer");
        ignorePatternSet.add("long");
        ignorePatternSet.add("Long");
        ignorePatternSet.add("java.lang.Long");
        ignorePatternSet.add("double");
        ignorePatternSet.add("Double");
        ignorePatternSet.add("java.lang.Double");
        ignorePatternSet.add("float");
        ignorePatternSet.add("Float");
        ignorePatternSet.add("java.lang.Float");
    }

    // cache these lookups in an LRU cache, as there are frequent duplicates (and the regex comparisons are much slower
    // than a cache lookup)
    private static LRUMap resultsCache = new LRUMap(8000);
    private static AtomicLong cacheLookupCount = new AtomicLong(0);
    private static AtomicLong cacheHitCount = new AtomicLong(0);

    static void clear()
    {
        patternsBySource.clear();
        patternsByLocation.clear();
        resultsCache.clear();
        cacheLookupCount.set(0);
        cacheHitCount.set(0);
    }

    /**
     * Register a regex pattern to filter interest in certain Java types.
     */
    public static void registerInterest(String sourceKey, String regex, List<TypeReferenceLocation> locations)
    {
        registerInterest(sourceKey, regex, locations.toArray(new TypeReferenceLocation[locations.size()]));
    }

    /**
     * Register a regex pattern to filter interest in certain Java types.
     */
    public static void registerInterest(String sourceKey, String regex, TypeReferenceLocation... locations)
    {
        patternsBySource.put(sourceKey, new PatternAndLocation(locations, regex));
    }

    private static String getCacheKey(TypeReferenceLocation location, String text)
    {
        return location + "_" + text;
    }

    private static Boolean checkCacheForMatches(String inputText, TypeReferenceLocation location)
    {
        String key = getCacheKey(location, inputText);
        Boolean cachedResult = (Boolean) resultsCache.get(key);
        Long lookupCount = cacheLookupCount.incrementAndGet();
        Long hitCount = cacheHitCount.get();
        if (cachedResult != null)
        {
            hitCount = cacheHitCount.incrementAndGet();
        }
        if (lookupCount % 100000L == 0)
        {
            long hitPercentage = Math.round(((double) hitCount / (double) lookupCount) * 100);
            LOG.info("There have been " + lookupCount + " lookups with " + hitCount
                        + " hits, for a hit percentage of: " + hitPercentage);
        }
        return cachedResult;
    }

    private static Map<String, Pattern> getPatternsByLocation(TypeReferenceLocation typeReferenceLocation)
    {
        Map<String, Pattern> result = patternsByLocation.get(typeReferenceLocation);
        if (result == null)
        {
            result = new HashMap<>();
            for (PatternAndLocation patternKey : patternsBySource.values())
            {
                String entryPattern = patternKey.pattern;
                TypeReferenceLocation[] entryLocations = patternKey.locations;
                if (result.containsKey(entryPattern))
                {
                    continue;
                }

                boolean shouldAdd = false;
                if (entryLocations == null || entryLocations.length == 0)
                {
                    shouldAdd = true;
                }
                else
                {
                    for (TypeReferenceLocation entryLocation : entryLocations)
                    {
                        if (typeReferenceLocation.equals(entryLocation))
                        {
                            shouldAdd = true;
                            break;
                        }
                    }
                }

                if (shouldAdd)
                {
                    /*
                     * For now, surround with .* to ensure that regexes will match some of the messier references that the type visitor report.
                     */
                    result.put(entryPattern, Pattern.compile(".*" + entryPattern + ".*"));
                }
            }
            patternsByLocation.put(typeReferenceLocation, result);
        }
        return result;
    }

    public static boolean matchesAny(String text, TypeReferenceLocation typeReferenceLocation)
    {
        ExecutionStatistics.get().begin("TypeInterestFactory.matchesAny(text)");
        try
        {
            if (ignorePatternSet.contains(text))
            {
                return false;
            }
            ExecutionStatistics.get().begin("TypeInterestFactory.matchesAny(text).cacheCheck");
            try
            {
                Boolean cachedResult = checkCacheForMatches(text, typeReferenceLocation);
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
                for (Pattern pattern : getPatternsByLocation(typeReferenceLocation).values())
                {
                    if (pattern.matcher(text).matches())
                    {
                        resultsCache.put(text, true);
                        return true;
                    }
                }
                resultsCache.put(getCacheKey(typeReferenceLocation, text), false);
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

    private static class PatternAndLocation
    {
        private TypeReferenceLocation[] locations;
        private String pattern;

        private PatternAndLocation(TypeReferenceLocation[] locations, String pattern)
        {
            this.locations = locations;
            this.pattern = pattern;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(locations);
            result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PatternAndLocation other = (PatternAndLocation) obj;
            if (!Arrays.equals(locations, other.locations))
                return false;
            if (pattern == null)
            {
                if (other.pattern != null)
                    return false;
            }
            else if (!pattern.equals(other.pattern))
                return false;
            return true;
        }
    }
}
