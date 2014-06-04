package org.apache.wicket.util.tester;

import org.apache.wicket.util.visit.*;
import org.apache.wicket.util.string.*;
import junit.framework.*;
import java.util.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.*;

public class WicketTesterHelper{
    public static List<ComponentData> getComponentData(final Page page){
        final List<ComponentData> data=(List<ComponentData>)new ArrayList();
        if(page!=null){
            page.visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
                public void component(final Component component,final IVisit<Void> visit){
                    final ComponentData object=new ComponentData();
                    String name=component.getClass().getName();
                    if(name.indexOf("$")>0){
                        name=component.getClass().getSuperclass().getName();
                    }
                    name=Strings.lastPathComponent(name,':');
                    object.path=component.getPageRelativePath();
                    object.type=name;
                    try{
                        object.value=component.getDefaultModelObjectAsString();
                    }
                    catch(Exception e){
                        object.value=e.getMessage();
                    }
                    data.add(object);
                }
            });
        }
        return data;
    }
    public static void assertEquals(final Collection<?> expects,final Collection<?> actuals){
        if(actuals.size()!=expects.size()||!expects.containsAll(actuals)||!actuals.containsAll(expects)){
            failWithVerboseMessage(expects,actuals);
        }
    }
    public static void failWithVerboseMessage(final Collection<?> expects,final Collection<?> actuals){
        Assert.fail("\nexpect ("+expects.size()+"):\n"+asLined(expects)+"\nbut was ("+actuals.size()+"):\n"+asLined(actuals));
    }
    public static String asLined(final Collection<?> objects){
        final StringBuilder lined=new StringBuilder();
        final Iterator<?> iter=(Iterator<?>)objects.iterator();
        while(iter.hasNext()){
            final String objectString=iter.next().toString();
            lined.append("   ");
            lined.append(objectString);
            if(iter.hasNext()){
                lined.append("\n");
            }
        }
        return lined.toString();
    }
    public static AjaxEventBehavior findAjaxEventBehavior(final Component component,final String event){
        for(final Behavior behavior : component.getBehaviors()){
            if(behavior instanceof AjaxEventBehavior&&event.equalsIgnoreCase(((AjaxEventBehavior)behavior).getEvent())){
                return (AjaxEventBehavior)behavior;
            }
        }
        return null;
    }
    public static Behavior findBehavior(final Component component,final Class<? extends Behavior> behaviorClass){
        for(final Behavior behavior : component.getBehaviors()){
            if(behaviorClass.isAssignableFrom(behavior.getClass())){
                return behavior;
            }
        }
        return null;
    }
    public static class ComponentData implements IClusterable{
        private static final long serialVersionUID=1L;
        public String path;
        public String type;
        public String value;
    }
}
