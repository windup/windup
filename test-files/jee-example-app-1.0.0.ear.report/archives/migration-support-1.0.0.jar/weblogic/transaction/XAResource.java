package weblogic.transaction;

public interface XAResource{
    boolean detectedUnavailable();
    int getDelistFlag();
}
