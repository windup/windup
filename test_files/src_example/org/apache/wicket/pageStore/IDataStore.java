package org.apache.wicket.pageStore;

public interface IDataStore{
    byte[] getData(String p0,int p1);
    void removeData(String p0,int p1);
    void removeData(String p0);
    void storeData(String p0,int p1,byte[] p2);
    void destroy();
    boolean isReplicated();
}
