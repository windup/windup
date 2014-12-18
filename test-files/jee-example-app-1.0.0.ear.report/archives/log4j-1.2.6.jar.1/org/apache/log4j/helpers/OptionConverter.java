package org.apache.log4j.helpers;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.Configurator;
import org.apache.log4j.spi.LoggerRepository;
import java.net.URL;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.Level;
import java.util.Properties;
import org.apache.log4j.helpers.LogLog;

public class OptionConverter{
    static String DELIM_START;
    static char DELIM_STOP;
    static int DELIM_START_LEN;
    static int DELIM_STOP_LEN;
    static /* synthetic */ Class class$java$lang$String;
    static /* synthetic */ Class class$org$apache$log4j$Level;
    static /* synthetic */ Class class$org$apache$log4j$spi$Configurator;
    public static String[] concatanateArrays(final String[] l,final String[] r){
        final int len=l.length+r.length;
        final String[] a=new String[len];
        System.arraycopy(l,0,a,0,l.length);
        System.arraycopy(r,0,a,l.length,r.length);
        return a;
    }
    public static String convertSpecialChars(final String s){
        final int len=s.length();
        final StringBuffer sbuf=new StringBuffer(len);
        int i=0;
        while(i<len){
            char c=s.charAt(i++);
            if(c=='\\'){
                c=s.charAt(i++);
                if(c=='n'){
                    c='\n';
                }
                else if(c=='r'){
                    c='\r';
                }
                else if(c=='t'){
                    c='\t';
                }
                else if(c=='f'){
                    c='\f';
                }
                else if(c=='\b'){
                    c='\b';
                }
                else if(c=='\"'){
                    c='\"';
                }
                else if(c=='\''){
                    c='\'';
                }
                else if(c=='\\'){
                    c='\\';
                }
            }
            sbuf.append(c);
        }
        return sbuf.toString();
    }
    public static String getSystemProperty(final String key,final String def){
        try{
            return System.getProperty(key,def);
        }
        catch(Throwable e){
            LogLog.debug("Was not allowed to read system property \""+key+"\".");
            return def;
        }
    }
    public static Object instantiateByKey(final Properties props,final String key,final Class superClass,final Object defaultValue){
        final String className=findAndSubst(key,props);
        if(className==null){
            LogLog.error("Could not find value for key "+key);
            return defaultValue;
        }
        return instantiateByClassName(className.trim(),superClass,defaultValue);
    }
    public static boolean toBoolean(final String value,final boolean dEfault){
        if(value==null){
            return dEfault;
        }
        final String trimmedVal=value.trim();
        return "true".equalsIgnoreCase(trimmedVal)||(!"false".equalsIgnoreCase(trimmedVal)&&dEfault);
    }
    public static int toInt(final String value,final int dEfault){
        if(value!=null){
            final String s=value.trim();
            try{
                return Integer.valueOf(s);
            }
            catch(NumberFormatException e){
                LogLog.error("["+s+"] is not in proper int form.");
                e.printStackTrace();
            }
        }
        return dEfault;
    }
    public static Level toLevel(final String value,final Level defaultValue){
        if(value==null){
            return defaultValue;
        }
        final int hashIndex=value.indexOf(35);
        if(hashIndex==-1){
            if("NULL".equalsIgnoreCase(value)){
                return null;
            }
            return Level.toLevel(value,defaultValue);
        }
        else{
            Level result=defaultValue;
            final String clazz=value.substring(hashIndex+1);
            final String levelName=value.substring(0,hashIndex);
            if("NULL".equalsIgnoreCase(levelName)){
                return null;
            }
            LogLog.debug("toLevel:class=["+clazz+"]"+":pri=["+levelName+"]");
            try{
                final Class customLevel=Loader.loadClass(clazz);
                final Class[] paramTypes= { (OptionConverter.class$java$lang$String==null)?(OptionConverter.class$java$lang$String=class$("java.lang.String")):OptionConverter.class$java$lang$String,(OptionConverter.class$org$apache$log4j$Level==null)?(OptionConverter.class$org$apache$log4j$Level=class$("org.apache.log4j.Level")):OptionConverter.class$org$apache$log4j$Level };
                final Method toLevelMethod=customLevel.getMethod("toLevel",(Class[])paramTypes);
                final Object[] params= { levelName,defaultValue };
                final Object o=toLevelMethod.invoke(null,params);
                result=(Level)o;
            }
            catch(ClassNotFoundException e6){
                LogLog.warn("custom level class ["+clazz+"] not found.");
            }
            catch(NoSuchMethodException e){
                LogLog.warn("custom level class ["+clazz+"]"+" does not have a constructor which takes one string parameter",e);
            }
            catch(InvocationTargetException e2){
                LogLog.warn("custom level class ["+clazz+"]"+" could not be instantiated",e2);
            }
            catch(ClassCastException e3){
                LogLog.warn("class ["+clazz+"] is not a subclass of org.apache.log4j.Level",e3);
            }
            catch(IllegalAccessException e4){
                LogLog.warn("class ["+clazz+"] cannot be instantiated due to access restrictions",e4);
            }
            catch(Exception e5){
                LogLog.warn("class ["+clazz+"], level ["+levelName+"] conversion failed.",e5);
            }
            return result;
        }
    }
    public static long toFileSize(final String value,final long dEfault){
        if(value==null){
            return dEfault;
        }
        String s=value.trim().toUpperCase();
        long multiplier=1L;
        int index;
        if((index=s.indexOf("KB"))!=-1){
            multiplier=1024L;
            s=s.substring(0,index);
        }
        else if((index=s.indexOf("MB"))!=-1){
            multiplier=1048576L;
            s=s.substring(0,index);
        }
        else if((index=s.indexOf("GB"))!=-1){
            multiplier=1073741824L;
            s=s.substring(0,index);
        }
        if(s!=null){
            try{
                return Long.valueOf(s)*multiplier;
            }
            catch(NumberFormatException e){
                LogLog.error("["+s+"] is not in proper int form.");
                LogLog.error("["+value+"] not in expected format.",e);
            }
        }
        return dEfault;
    }
    public static String findAndSubst(final String key,final Properties props){
        final String value=props.getProperty(key);
        if(value==null){
            return null;
        }
        try{
            return substVars(value,props);
        }
        catch(IllegalArgumentException e){
            LogLog.error("Bad option value ["+value+"].",e);
            return value;
        }
    }
    public static Object instantiateByClassName(final String className,final Class superClass,final Object defaultValue){
        if(className!=null){
            try{
                final Class loadClass=Loader.loadClass(className);
                if(!superClass.isAssignableFrom(loadClass)){
                    LogLog.error("A \""+className+"\" object is not assignable to a \""+superClass.getName()+"\" variable.");
                    LogLog.error("The class \""+superClass.getName()+"\" was loaded by ");
                    LogLog.error("["+superClass.getClassLoader()+"] whereas object of type ");
                    LogLog.error("\""+loadClass.getName()+"\" was loaded by ["+loadClass.getClassLoader()+"].");
                    return defaultValue;
                }
                return loadClass.newInstance();
            }
            catch(Exception e){
                LogLog.error("Could not instantiate class ["+className+"].",e);
            }
        }
        return defaultValue;
    }
    public static String substVars(final String val,final Properties props) throws IllegalArgumentException{
        final StringBuffer sbuf=new StringBuffer();
        int i=0;
        while(true){
            int j=val.indexOf(OptionConverter.DELIM_START,i);
            if(j==-1){
                if(i==0){
                    return val;
                }
                sbuf.append(val.substring(i,val.length()));
                return sbuf.toString();
            }
            else{
                sbuf.append(val.substring(i,j));
                final int k=val.indexOf(OptionConverter.DELIM_STOP,j);
                if(k==-1){
                    throw new IllegalArgumentException('\"'+val+"\" has no closing brace. Opening brace at position "+j+'.');
                }
                j+=OptionConverter.DELIM_START_LEN;
                final String key=val.substring(j,k);
                String replacement=getSystemProperty(key,null);
                if(replacement==null&&props!=null){
                    replacement=props.getProperty(key);
                }
                if(replacement!=null){
                    final String recursiveReplacement=substVars(replacement,props);
                    sbuf.append(recursiveReplacement);
                }
                i=k+OptionConverter.DELIM_STOP_LEN;
            }
        }
    }
    public static void selectAndConfigure(final URL url,String clazz,final LoggerRepository hierarchy){
        Configurator configurator=null;
        final String filename=url.getFile();
        if(clazz==null&&filename!=null&&filename.endsWith(".xml")){
            clazz="org.apache.log4j.xml.DOMConfigurator";
        }
        if(clazz!=null){
            LogLog.debug("Preferred configurator class: "+clazz);
            configurator=(Configurator)instantiateByClassName(clazz,(OptionConverter.class$org$apache$log4j$spi$Configurator==null)?(OptionConverter.class$org$apache$log4j$spi$Configurator=class$("org.apache.log4j.spi.Configurator")):OptionConverter.class$org$apache$log4j$spi$Configurator,null);
            if(configurator==null){
                LogLog.error("Could not instantiate configurator ["+clazz+"].");
                return;
            }
        }
        else{
            configurator=new PropertyConfigurator();
        }
        configurator.doConfigure(url,hierarchy);
    }
    static /* synthetic */ Class class$(final String x0){
        try{
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x){
            throw new NoClassDefFoundError(x.getMessage());
        }
    }
    static{
        OptionConverter.DELIM_START="${";
        OptionConverter.DELIM_STOP='}';
        OptionConverter.DELIM_START_LEN=2;
        OptionConverter.DELIM_STOP_LEN=1;
    }
}
