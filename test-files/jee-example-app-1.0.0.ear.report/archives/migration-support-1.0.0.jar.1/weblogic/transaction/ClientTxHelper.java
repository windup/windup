package weblogic.transaction;

import org.migration.support.NotImplemented;
import weblogic.transaction.TransactionManager;
import weblogic.transaction.TxHelper;

public class ClientTxHelper extends TxHelper{
    public static TransactionManager getTransactionManager(){
        throw new NotImplemented();
    }
}
