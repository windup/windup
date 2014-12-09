package org.jboss.windup.rules.apps.xml.condition;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Maintains a cache of XPath's that have been run against files in the application. If there were no results for a particular XPath, keep track of
 * that so that we do not have to parse the file again.
 * 
 * @author jsightler
 *
 */
public class XmlFileCache
{
    private static Set<CacheKey> matchCache = new HashSet<>();

    /**
     * Returns false if we have previously indicated that this xpath does not match for this file.
     * 
     * Returns true otherwise.
     */
    public static boolean matchIsPossible(Object xmlID, String xpath, Map<String, String> namespaces)
    {
        synchronized (matchCache)
        {
            CacheKey cacheKey = new CacheKey(xmlID, xpath, namespaces);
            return !matchCache.contains(cacheKey);
        }
    }

    public static void cacheNoResultsFound(Object xmlID, String xpath, Map<String, String> namespaces)
    {
        synchronized (matchCache)
        {
            CacheKey cacheKey = new CacheKey(xmlID, xpath, namespaces);
            matchCache.add(cacheKey);
        }
    }

    private static class CacheKey
    {
        private Object xmlFileID;
        private String xpath;
        private Map<String, String> namespaces;

        public CacheKey(Object xmlFileID, String xpath, Map<String, String> namespaces)
        {
            super();
            this.xmlFileID = xmlFileID;
            this.xpath = xpath;
            this.namespaces = namespaces;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((namespaces == null) ? 0 : namespaces.hashCode());
            result = prime * result + ((xmlFileID == null) ? 0 : xmlFileID.hashCode());
            result = prime * result + ((xpath == null) ? 0 : xpath.hashCode());
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
            CacheKey other = (CacheKey) obj;
            if (namespaces == null)
            {
                if (other.namespaces != null)
                    return false;
            }
            else if (!namespaces.equals(other.namespaces))
                return false;
            if (xmlFileID == null)
            {
                if (other.xmlFileID != null)
                    return false;
            }
            else if (!xmlFileID.equals(other.xmlFileID))
                return false;
            if (xpath == null)
            {
                if (other.xpath != null)
                    return false;
            }
            else if (!xpath.equals(other.xpath))
                return false;
            return true;
        }

    }
}
