package org.apache.log4j.config;

import java.io.OutputStream;
import org.apache.log4j.Level;
import org.apache.log4j.Appender;
import java.util.Enumeration;
import org.apache.log4j.Category;
import java.io.PrintWriter;
import java.util.Hashtable;
import org.apache.log4j.config.PropertyGetter;

public class PropertyPrinter implements PropertyGetter.PropertyCallback{
    protected int numAppenders;
    protected Hashtable appenderNames;
    protected Hashtable layoutNames;
    protected PrintWriter out;
    protected boolean doCapitalize;
    public PropertyPrinter(final PrintWriter out){
        this(out,false);
    }
    public PropertyPrinter(final PrintWriter out,final boolean doCapitalize){
        super();
        this.numAppenders=0;
        this.appenderNames=new Hashtable();
        this.layoutNames=new Hashtable();
        this.out=out;
        this.doCapitalize=doCapitalize;
        this.print(out);
        out.flush();
    }
    protected String genAppName(){
        return "A"+this.numAppenders++;
    }
    protected boolean isGenAppName(final String name){
        if(name.length()<2||name.charAt(0)!='A'){
            return false;
        }
        for(int i=0;i<name.length();++i){
            if(name.charAt(i)<'0'||name.charAt(i)>'9'){
                return false;
            }
        }
        return true;
    }
    public void print(final PrintWriter out){
        this.printOptions(out,Category.getRoot());
        final Enumeration cats=Category.getCurrentCategories();
        while(cats.hasMoreElements()){
            this.printOptions(out,cats.nextElement());
        }
    }
    protected void printOptions(final PrintWriter out,final Category cat){
        final Enumeration appenders=cat.getAllAppenders();
        final Level prio=cat.getLevel();
        String appenderString=(prio==null)?"":prio.toString();
        while(appenders.hasMoreElements()){
            final Appender app=appenders.nextElement();
            String name;
            if((name=this.appenderNames.get(app))==null){
                if((name=app.getName())==null||this.isGenAppName(name)){
                    name=this.genAppName();
                }
                this.appenderNames.put(app,name);
                this.printOptions(out,app,"log4j.appender."+name);
                if(app.getLayout()!=null){
                    this.printOptions(out,app.getLayout(),"log4j.appender."+name+".layout");
                }
            }
            appenderString=appenderString+", "+name;
        }
        final String catKey=(cat==Category.getRoot())?"log4j.rootCategory":("log4j.category."+cat.getName());
        if(appenderString!=""){
            out.println(catKey+"="+appenderString);
        }
    }
    protected void printOptions(final PrintWriter out,final Object obj,final String fullname){
        out.println(fullname+"="+obj.getClass().getName());
        PropertyGetter.getProperties(obj,this,fullname+".");
    }
    public void foundProperty(final Object obj,final String prefix,String name,final Object value){
        if(obj instanceof Appender&&"name".equals(name)){
            return;
        }
        if(this.doCapitalize){
            name=capitalize(name);
        }
        this.out.println(prefix+name+"="+value.toString());
    }
    public static String capitalize(final String name){
        if(Character.isLowerCase(name.charAt(0))&&(name.length()==1||Character.isLowerCase(name.charAt(1)))){
            final StringBuffer newname=new StringBuffer(name);
            newname.setCharAt(0,Character.toUpperCase(name.charAt(0)));
            return newname.toString();
        }
        return name;
    }
    public static void main(final String[] args){
        new PropertyPrinter(new PrintWriter(System.out));
    }
}
