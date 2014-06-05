package org.apache.wicket.protocol.http.mock;

import javax.servlet.http.*;
import org.apache.wicket.util.lang.*;

public final class Cookies{
    public static Cookie copyOf(final Cookie cookie){
        return (cookie!=null)?((Cookie)cookie.clone()):null;
    }
    public static boolean isEqual(final Cookie c1,final Cookie c2){
        Args.notNull((Object)c1,"c1");
        Args.notNull((Object)c2,"c2");
        return c1.getName().equals(c2.getName())&&((c1.getPath()==null&&c2.getPath()==null)||c1.getPath().equals(c2.getPath()))&&((c1.getDomain()==null&&c2.getDomain()==null)||c1.getDomain().equals(c2.getDomain()));
    }
}
