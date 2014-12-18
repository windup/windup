package org.apache.log4j.lf5.util;

import java.net.URL;
import java.io.InputStream;
import org.apache.log4j.lf5.util.Resource;

public class ResourceUtils{
    public static InputStream getResourceAsStream(final Object object,final Resource resource){
        final ClassLoader loader=object.getClass().getClassLoader();
        InputStream in=null;
        if(loader!=null){
            in=loader.getResourceAsStream(resource.getName());
        }
        else{
            in=ClassLoader.getSystemResourceAsStream(resource.getName());
        }
        return in;
    }
    public static URL getResourceAsURL(final Object object,final Resource resource){
        final ClassLoader loader=object.getClass().getClassLoader();
        URL url=null;
        if(loader!=null){
            url=loader.getResource(resource.getName());
        }
        else{
            url=ClassLoader.getSystemResource(resource.getName());
        }
        return url;
    }
}
