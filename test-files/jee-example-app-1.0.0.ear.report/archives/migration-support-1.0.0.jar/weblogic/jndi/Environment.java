package weblogic.jndi;

import java.io.ObjectOutput;
import java.io.InputStream;
import java.io.ObjectInput;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import javax.net.ssl.TrustManager;
import weblogic.security.acl.UserInfo;
import java.util.Hashtable;
import javax.naming.Context;
import org.migration.support.NotImplemented;

public class Environment{
    public boolean getBoolean(final String name,final boolean defaultValue){
        throw new NotImplemented();
    }
    public Context getContext(){
        throw new NotImplemented();
    }
    public Context getContext(final String contextName){
        throw new NotImplemented();
    }
    public boolean getCreateIntermediateContexts(){
        throw new NotImplemented();
    }
    public Hashtable getDelegateEnvironment(){
        throw new NotImplemented();
    }
    public boolean getEnableDefaultUser(){
        throw new NotImplemented();
    }
    public boolean getEnableServerAffinity(){
        throw new NotImplemented();
    }
    public Context getInitialContext(){
        throw new NotImplemented();
    }
    public String getInitialContextFactory(){
        throw new NotImplemented();
    }
    public boolean getPinToPrimaryServer(){
        throw new NotImplemented();
    }
    public Hashtable getProperties(){
        throw new NotImplemented();
    }
    public Object getProperty(final String name){
        throw new NotImplemented();
    }
    public Object getPropertyFromEnv(final String name){
        throw new NotImplemented();
    }
    public String getProviderUrl(){
        throw new NotImplemented();
    }
    public boolean getReplicateBindings(){
        throw new NotImplemented();
    }
    public long getRequestTimeout(){
        throw new NotImplemented();
    }
    public long getRMIClientTimeout(){
        throw new NotImplemented();
    }
    public Object getSecurityCredentials(){
        throw new NotImplemented();
    }
    public String getSecurityPrincipal(){
        throw new NotImplemented();
    }
    public UserInfo getSecurityUser(){
        throw new NotImplemented();
    }
    public Object getSSLClientCertificate(){
        throw new NotImplemented();
    }
    public Object getSSLClientKeyPassword(){
        throw new NotImplemented();
    }
    public TrustManager getSSLClientTrustManager(){
        throw new NotImplemented();
    }
    public byte[][] getSSLRootCAFingerprints(){
        throw new NotImplemented();
    }
    public String getSSLServerName(){
        throw new NotImplemented();
    }
    public String getString(final String name){
        throw new NotImplemented();
    }
    public void loadLocalIdentity(final Certificate[] certs,final PrivateKey privateKey){
        throw new NotImplemented();
    }
    public void readExternal(final ObjectInput in){
        throw new NotImplemented();
    }
    public Object removeProperty(final String name){
        throw new NotImplemented();
    }
    public void setBoolean(final String name,final boolean value){
        throw new NotImplemented();
    }
    public void setCreateIntermediateContexts(final boolean flag){
        throw new NotImplemented();
    }
    public void setDelegateEnvironment(final Hashtable delegateEnv){
        throw new NotImplemented();
    }
    public void setEnableDefaultUser(final boolean defaultUser){
        throw new NotImplemented();
    }
    public void setEnableServerAffinity(final boolean enable){
        throw new NotImplemented();
    }
    public void setInitialContextFactory(final String factoryName){
        throw new NotImplemented();
    }
    public void setPinToPrimaryServer(final boolean enable){
        throw new NotImplemented();
    }
    public Object setProperty(final String name,final Object value){
        throw new NotImplemented();
    }
    public void setProviderUrl(final String url){
        throw new NotImplemented();
    }
    public void setProviderURL(final String url){
        throw new NotImplemented();
    }
    public void setReplicateBindings(final boolean enable){
        throw new NotImplemented();
    }
    public void setRequestTimeout(final long timeout){
        throw new NotImplemented();
    }
    public void setRMIClientTimeout(final long timeout){
        throw new NotImplemented();
    }
    public void setSecurityCredentials(final Object credentials){
        throw new NotImplemented();
    }
    public void setSecurityPrincipal(final String principal){
        throw new NotImplemented();
    }
    public void setSecurityUser(final UserInfo user){
        throw new NotImplemented();
    }
    public void setSSLClientCertificate(final InputStream[] chain){
        throw new NotImplemented();
    }
    public void setSSLClientKeyPassword(final String pass){
        throw new NotImplemented();
    }
    public void setSSLClientTrustManager(final TrustManager trustManager){
        throw new NotImplemented();
    }
    public void setSSLRootCAFingerprints(final byte[][] fps){
        throw new NotImplemented();
    }
    public void setSSLRootCAFingerprints(final String fps){
        throw new NotImplemented();
    }
    public void setSSLServerName(final String name){
        throw new NotImplemented();
    }
    public void writeExternal(final ObjectOutput out){
        throw new NotImplemented();
    }
}
