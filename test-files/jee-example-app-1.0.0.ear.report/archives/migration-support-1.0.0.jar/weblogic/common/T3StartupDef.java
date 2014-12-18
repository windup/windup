package weblogic.common;

import java.util.Hashtable;
import weblogic.common.T3ServicesDef;

public interface T3StartupDef{
    void setServices(T3ServicesDef p0);
    String startup(String p0,Hashtable p1);
}
