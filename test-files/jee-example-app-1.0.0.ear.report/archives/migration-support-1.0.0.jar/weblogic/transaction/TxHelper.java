package weblogic.transaction;

import weblogic.transaction.UserTransaction;
import weblogic.transaction.Transaction;
import weblogic.transaction.ClientTransactionManager;
import weblogic.transaction.InterposedTransactionManager;
import javax.naming.Context;
import org.migration.support.NotImplemented;
import javax.transaction.xa.Xid;

public class TxHelper{
    public static Xid createXid(final byte[] aGlobalTransactionId,final byte[] aBranchQualifier){
        throw new NotImplemented();
    }
    public static Xid createXid(final int aFormatId,final byte[] aGlobalTransactionId,final byte[] aBranchQualifier){
        throw new NotImplemented();
    }
    public static InterposedTransactionManager getClientInterposedTransactionManager(final Context initialContext,final String serverName){
        throw new NotImplemented();
    }
    public static ClientTransactionManager getClientTransactionManager(){
        throw new NotImplemented();
    }
    public static InterposedTransactionManager getServerInterposedTransactionManager(){
        throw new NotImplemented();
    }
    public static Transaction getTransaction(){
        throw new NotImplemented();
    }
    public static UserTransaction getUserTransaction(){
        throw new NotImplemented();
    }
    public static String status2String(final int status){
        throw new NotImplemented();
    }
}
