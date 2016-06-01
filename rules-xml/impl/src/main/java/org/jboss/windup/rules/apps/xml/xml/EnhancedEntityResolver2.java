package org.jboss.windup.rules.apps.xml.xml;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

/**
 * This is an {@link EntityResolver2} that has been enhanced to support URL redirection.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class EnhancedEntityResolver2 implements EntityResolver2
{
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
        URL url = baseURI != null ? new URL(new URL(baseURI), systemId) : new URL(systemId);

        URLConnection connection = url.openConnection();

        // Not a HTTP connection... skip redirection logic
        if (!(connection instanceof HttpURLConnection))
            return new InputSource(connection.getInputStream());

        HttpURLConnection httpConnection = (HttpURLConnection)connection;

        int status = httpConnection.getResponseCode();
        if ((status != HttpURLConnection.HTTP_OK) &&
                    (status == HttpURLConnection.HTTP_MOVED_TEMP
                                || status == HttpURLConnection.HTTP_MOVED_PERM
                                || status == HttpURLConnection.HTTP_SEE_OTHER))
        {
            String newUrl = httpConnection.getHeaderField("Location");
            httpConnection = (HttpURLConnection) new URL(newUrl).openConnection();
        }

        InputSource inputSource = new InputSource(httpConnection.getInputStream());
        inputSource.setSystemId(url.toString());
        inputSource.setPublicId(publicId);
        return inputSource;

    }

}
