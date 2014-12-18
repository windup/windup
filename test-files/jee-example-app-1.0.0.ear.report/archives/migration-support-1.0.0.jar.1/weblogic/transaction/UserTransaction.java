package weblogic.transaction;

public interface UserTransaction{
    void begin(String p0);
    void begin(String p0,int p1);
}
