package weblogic.transaction;

import weblogic.transaction.Transaction;

public interface ClientTransactionManager{
    void forceResume(Transaction p0);
    Transaction forceSuspend();
}
