package org.jboss.windup.rules.apps.xml.model;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.windup.util.Logging;
import org.w3c.dom.Document;

/**
 * This class provides a cache for parsed XML documents, using {@link SoftReference}s in order to avoid creating unnecessary memory pressure.
 */
public class XMLDocumentCache {
    private static final Logger LOG = Logging.get(XMLDocumentCache.class);
    private static final Map<String, CacheDocument> map = new HashMap<>();

    /**
     * This is used to pass data back to the caller regarding the cache search.
     * <p>
     * If a parsing failure had been cached, {@link Result#isParseFailure()} will return true, and {@link Result#getDocument()} will return false.
     * <p>
     * A cache miss will have {@link Result#isParseFailure()} set to false and {@link Result#getDocument()} will be null.
     */
    public static class Result {
        private final boolean parseFailure;
        private final Document document;

        public Result(boolean parseFailure, Document document) {
            this.parseFailure = parseFailure;
            this.document = document;
        }

        public boolean isParseFailure() {
            return parseFailure;
        }

        public Document getDocument() {
            return document;
        }
    }

    /**
     * Add the provided document to the cache.
     */
    public static void cache(XmlFileModel key, Document document) {
        String cacheKey = getKey(key);
        map.put(cacheKey, new CacheDocument(false, document));
    }

    /**
     * Cache a parse failure for this document.
     */
    public static void cacheParseFailure(XmlFileModel key) {
        map.put(getKey(key), new CacheDocument(true, null));
    }

    /**
     * Retrieve the currently cached value for the given document.
     */
    public static Result get(XmlFileModel key) {
        String cacheKey = getKey(key);

        Result result = null;
        CacheDocument reference = map.get(cacheKey);

        if (reference == null)
            return new Result(false, null);

        if (reference.parseFailure)
            return new Result(true, null);

        Document document = reference.getDocument();
        if (document == null)
            LOG.info("Cache miss on XML document: " + cacheKey);

        return new Result(false, document);
    }

    private static String getKey(XmlFileModel key) {
        return key.getFilePath();
    }

    private static class CacheDocument {
        private boolean parseFailure;
        private final SoftReference<Document> document;

        public CacheDocument(boolean parseFailure, Document document) {
            this.parseFailure = parseFailure;
            this.document = new SoftReference<>(document);
        }

        public Document getDocument() {
            return document.get();
        }
    }
}
