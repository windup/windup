package weblogic.transaction;

import weblogic.transaction.UserTransaction;
import weblogic.transaction.ClientTransactionManager;
import javax.transaction.Transaction;
import org.migration.support.NotImplemented;

public abstract class TransactionHelper{
    public TransactionHelper(){
        super();
        throw new NotImplemented();
    }
    public Transaction getTransaction(){
        throw new NotImplemented();
    }
    public static TransactionHelper getTransactionHelper(){
        throw new NotImplemented();
    }
    public abstract ClientTransactionManager getTransactionManager();
    public abstract UserTransaction getUserTransaction();
    public static TransactionHelper popTransactionHelper(){
        throw new NotImplemented();
    }
    public static void pushTransactionHelper(final TransactionHelper helper){
        throw new NotImplemented();
    }
    public static void setTransactionHelper(final TransactionHelper helper){
        throw new NotImplemented();
    }
}
