package org.jboss.windup.qs.identarch.lib;

import org.jboss.windup.qs.identarch.model.GAV;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Queries online HTTP source for GAV info.
 * To be finished and tested.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public abstract class HttpBaseIdentifier implements HashToGAVIdentifier
{
    // Input.
    private final String baseURL;

    // Work tools.
    private final HttpClient client = new HttpClient();


    /**
     * @param baseURL  Base URL of the endpoint, in which the ${SHA1} token will be replaced with the hash.
     *                 E.g. https://repository.jboss.org/nexus/service/local/identify/sha1/${SHA1}
     *                 or   http://search.maven.org/solrsearch/select?q=1:"35379fb6526fd019f331542b4e9ae2e566c57933"&wt=json
     *                 or   http://search.maven.org/solrsearch/select?q=1:"35379fb6526fd019f331542b4e9ae2e566c57933"&wt=xml
     */
    public HttpBaseIdentifier(String baseURL)
    {
        this.baseURL = baseURL;
    }

    public abstract GAV parseResponse(String contentType, String responseBody);


    @Override
    public GAV getGAVFromSHA1(String sha1Hash)
    {
        String url = baseURL.replace("${SHA1}", sha1Hash);
        try
        {
            /*// org.apache.httpcomponents - Maven repos are somehow screwed up, can't download.
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("http://localhost/");
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
            } finally {
                response.close();
            }
            /**/

            // org.apache.commons.httpclient (obsolete)
            final GetMethod request = new GetMethod(url);
            int result = client.executeMethod(request);
            String responseBody = request.getResponseBodyAsString(MAX_ENTRY_LENGTH);
            String contentType = request.getResponseHeader("Content-Type").getValue();
            GAV gav = this.parseResponse(contentType, responseBody);

            return gav;
        }
        catch( Throwable ex )
        {
            throw new RuntimeException("Failed quering the URL for SHA1 "+ sha1Hash +" to GAV mapping:\n\t" + url + "\n\t" + ex.getMessage(), ex);
        }
    }

}// class
