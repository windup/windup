package org.apache.wicket.authentication;

public interface IAuthenticationStrategy{
    String[] load();
    void save(String p0,String p1);
    void remove();
}
