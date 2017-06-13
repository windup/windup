package org.jboss.windup.rules.apps.xml.xml;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

/**
 * This is an {@link EntityResolver2} that has been enhanced to support URL redirection.
 *
 * It also use local stored DTDs/XSDs as possible not to slow down validation.
 * see https://www.w3.org/blog/systeam/2008/02/08/w3c_s_excessive_dtd_traffic/
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 */
public class EnhancedEntityResolver2 implements EntityResolver2
{
    private static final Logger LOG = Logger.getLogger(EnhancedEntityResolver2.class.getName());

    /*
     * This is catalog resolver definition for resolving entities/schemas locally instead of remotely
     * default catalogs settings are in CatalogManager.properties file available on classpath,
     * There is a default settings in windup/rules-xml-api module as rules-xml/api/src/main/resources/CatalogManager.properties
     * and there is also default catalog with HTML/XHTML/XML local definitions.
     *
     */
    private final static CatalogResolver catalogResolver = new CatalogResolver();

    private boolean onlineMode;

    public EnhancedEntityResolver2(boolean onlineMode)
    {
        this.onlineMode = onlineMode;
    }

    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException
    {
        return null;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
    {
        return resolveEntity(null, publicId, null, systemId);
    }

    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException
    {
        LOG.fine("Entity for resolving " + publicId + " " + systemId);
        // try to resolve first from catalog if it doesn't find entity continue with other ways
        InputSource inputSource = catalogResolver.resolveEntity(publicId, systemId);
        if (inputSource != null)
        {
            LOG.fine("Resolved entity through catalog.");
            return inputSource;
        }

        URL url = baseURI != null ? new URL(new URL(baseURI), systemId) : new URL(systemId);
        LOG.fine("Resolving entity -> " + url.toString());
        if (!onlineMode && ValidateXmlHandler.isNetworkUrl(url.toExternalForm()))
            throw new IOException("XSD Not accessible in offline mode!");

        URLConnection connection = url.openConnection();

        // Not a HTTP connection... skip redirection logic
        if (!(connection instanceof HttpURLConnection))
            return new InputSource(connection.getInputStream());

        HttpURLConnection httpConnection = (HttpURLConnection)connection;

        int status = httpConnection.getResponseCode();
        for (int i = 0; i < 4 && isRedirect(status); i++)
        {
            String newUrl = httpConnection.getHeaderField("Location");
            httpConnection = (HttpURLConnection) new URL(newUrl).openConnection();
            httpConnection.setConnectTimeout(10000);
            httpConnection.setReadTimeout(10000);
            status = httpConnection.getResponseCode();
        }

        inputSource = new InputSource(httpConnection.getInputStream());
        inputSource.setSystemId(url.toString());
        inputSource.setPublicId(publicId);
        return inputSource;

    }

    private boolean isRedirect(int status)
    {
        return (status != HttpURLConnection.HTTP_OK) &&
                (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER);
    }

}
