package org.jboss.windup.rules.apps.xml.model;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.windup.util.Logging;
import org.w3c.dom.Document;

public class XMLDocumentCache
{
    private static final Logger LOG = Logging.get(XMLDocumentCache.class);
    private static final Map<String, SoftReference<Document>> map = new HashMap<String, SoftReference<Document>>();

    public static void put(XmlFileModel key, Document document)
    {
        String cacheKey = getKey(key);
        map.put(cacheKey, new SoftReference<Document>(document));
    }

    public static Document get(XmlFileModel key)
    {
        String cacheKey = getKey(key);

        Document result = null;
        SoftReference<Document> ref = map.get(cacheKey);
        if (ref != null)
            result = ref.get();

        if (result == null)
            LOG.info("Cache miss on XML document: " + cacheKey);

        return result;
    }

    private static String getKey(XmlFileModel key)
    {
        return key.getFilePath();
    }
}
