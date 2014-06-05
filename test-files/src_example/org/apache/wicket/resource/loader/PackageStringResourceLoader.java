package org.apache.wicket.resource.loader;

import java.util.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.resource.locator.*;
import org.apache.wicket.resource.*;
import org.slf4j.*;

public class PackageStringResourceLoader extends ComponentStringResourceLoader{
    private static final Logger log;
    private String filename;
    public PackageStringResourceLoader(){
        super();
        this.filename="package";
    }
    public String loadStringResource(Class<?> clazz,final String key,final Locale locale,final String style,final String variation){
        if(clazz==null){
            return null;
        }
        final IPropertiesFactory propertiesFactory=Application.get().getResourceSettings().getPropertiesFactory();
        while(true){
            final Package pkg=clazz.getPackage();
            String packageName=(pkg==null)?"":pkg.getName();
            packageName=packageName.replace('.','/');
            do{
                String path=this.filename;
                if(packageName.length()>0){
                    path=packageName+"/"+path;
                }
                final ResourceNameIterator iter=this.newResourceNameIterator(path,locale,style,variation);
                while(iter.hasNext()){
                    final String newPath=iter.next();
                    final Properties props=propertiesFactory.load(clazz,newPath);
                    if(props!=null){
                        final String value=props.getString(key);
                        if(value!=null){
                            return value;
                        }
                        continue;
                    }
                }
                packageName=Strings.beforeLast(packageName,'/');
            } while(packageName.length()>0);
            clazz=(Class<?>)clazz.getSuperclass();
            if(clazz==null){
                return null;
            }
        }
    }
    public String getFilename(){
        return this.filename;
    }
    public void setFilename(final String filename){
        this.filename=filename;
    }
    static{
        log=LoggerFactory.getLogger(PackageStringResourceLoader.class);
    }
}
