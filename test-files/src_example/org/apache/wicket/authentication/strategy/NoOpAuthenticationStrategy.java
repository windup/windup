package org.apache.wicket.authentication.strategy;

import org.apache.wicket.authentication.*;

public class NoOpAuthenticationStrategy implements IAuthenticationStrategy{
    public String[] load(){
        return null;
    }
    public void save(final String username,final String password){
    }
    public void remove(){
    }
}
