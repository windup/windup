package org.apache.wicket.util.crypt;

import org.apache.wicket.*;
import java.util.*;
import java.io.*;

public class KeyInSessionSunJceCryptFactory implements ICryptFactory{
    private static MetaDataKey<String> KEY;
    public ICrypt newCrypt(){
        final Session session=Session.get();
        session.bind();
        String key=session.getMetaData(KeyInSessionSunJceCryptFactory.KEY);
        if(key==null){
            key=session.getId()+"."+UUID.randomUUID().toString();
            session.setMetaData(KeyInSessionSunJceCryptFactory.KEY,key);
        }
        final ICrypt crypt=(ICrypt)new SunJceCrypt();
        crypt.setKey(key);
        return crypt;
    }
    static{
        KeyInSessionSunJceCryptFactory.KEY=new MetaDataKey<String>(){
            private static final long serialVersionUID=1L;
        };
    }
}
