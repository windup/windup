package org.apache.wicket.protocol.https;

public class HttpsConfig{
    private int httpPort;
    private int httpsPort;
    private boolean preferStateful;
    public HttpsConfig(){
        this(80,443);
    }
    public HttpsConfig(final int httpPort,final int httpsPort){
        super();
        this.preferStateful=true;
        this.httpPort=httpPort;
        this.httpsPort=httpsPort;
    }
    public void setHttpPort(final int httpPort){
        this.httpPort=httpPort;
    }
    public void setHttpsPort(final int httpsPort){
        this.httpsPort=httpsPort;
    }
    public int getHttpPort(){
        return this.httpPort;
    }
    public int getHttpsPort(){
        return this.httpsPort;
    }
    public boolean isPreferStateful(){
        return this.preferStateful;
    }
    public void setPreferStateful(final boolean preferStateful){
        this.preferStateful=preferStateful;
    }
}
