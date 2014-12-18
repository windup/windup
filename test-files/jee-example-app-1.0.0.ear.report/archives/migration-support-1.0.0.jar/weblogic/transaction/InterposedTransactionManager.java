package weblogic.transaction;

import weblogic.transaction.XAResource;
import javax.transaction.xa.Xid;
import weblogic.transaction.Transaction;

public interface InterposedTransactionManager{
    Transaction getTransaction();
    Transaction getTransaction(Xid p0);
    XAResource getXAResource();
}
