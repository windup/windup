package weblogic.transaction;

import java.util.Hashtable;
import weblogic.transaction.XAResource;
import weblogic.transaction.nonxa.NonXAResource;
import weblogic.transaction.Transaction;
import javax.transaction.xa.Xid;

public interface TransactionManager{
    void begin(int p0);
    void begin(String p0);
    void begin(String p0,int p1);
    Transaction getTransaction(Xid p0);
    Transaction getTransaction();
    void registerDynamicResource(String p0,NonXAResource p1);
    void registerDynamicResource(String p0,XAResource p1);
    void registerResource(String p0,XAResource p1);
    void registerResource(String p0,XAResource p1,Hashtable p2);
    void registerStaticResource(String p0,XAResource p1);
    void unregisterResource(String p0);
    void unregisterResource(String p0,boolean p1);
}
