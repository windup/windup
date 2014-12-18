package weblogic.transaction;

import javax.transaction.xa.Xid;
import java.io.Serializable;
import weblogic.transaction.nonxa.NonXAResource;
import java.util.Map;
import javax.transaction.Transaction;

public interface Transaction extends javax.transaction.Transaction{
    void addProperties(Map p0);
    boolean enlistResource(NonXAResource p0);
    String getHeuristicErrorMessage();
    long getMillisSinceBegin();
    String getName();
    Map getProperties();
    Serializable getProperty(String p0);
    Throwable getRollbackReason();
    String getStatusAsString();
    long getTimeToLiveMillis();
    Xid getXid();
    Xid getXID();
    boolean isTimedOut();
    boolean isTxAsyncTimeout();
    void setName(String p0);
    void setProperty(String p0,Serializable p1);
    void setRollbackOnly(String p0,Throwable p1);
    void setRollbackOnly(Throwable p0);
}
