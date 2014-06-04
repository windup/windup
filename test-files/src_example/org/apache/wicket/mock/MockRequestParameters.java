package org.apache.wicket.mock;

import org.apache.wicket.request.*;
import org.apache.wicket.util.string.*;
import java.util.*;

public class MockRequestParameters implements IWritableRequestParameters{
    private final Map<String,List<StringValue>> parameters;
    public MockRequestParameters(){
        super();
        this.parameters=(Map<String,List<StringValue>>)new HashMap();
    }
    public Set<String> getParameterNames(){
        return (Set<String>)Collections.unmodifiableSet(this.parameters.keySet());
    }
    public StringValue getParameterValue(final String name){
        final List<StringValue> values=(List<StringValue>)this.parameters.get(name);
        return (StringValue)((values!=null&&!values.isEmpty())?values.get(0):StringValue.valueOf((String)null));
    }
    public List<StringValue> getParameterValues(final String name){
        final List<StringValue> values=(List<StringValue>)this.parameters.get(name);
        return (List<StringValue>)((values!=null)?Collections.unmodifiableList(values):null);
    }
    public void setParameterValues(final String name,final List<StringValue> values){
        this.parameters.put(name,values);
    }
    public void setParameterValue(final String name,final String value){
        final List<StringValue> list=(List<StringValue>)new ArrayList(1);
        list.add(StringValue.valueOf(value));
        this.parameters.put(name,list);
    }
    public void addParameterValue(final String name,final String value){
        List<StringValue> list=(List<StringValue>)this.parameters.get(name);
        if(list==null){
            list=(List<StringValue>)new ArrayList(1);
            this.parameters.put(name,list);
        }
        list.add(StringValue.valueOf(value));
    }
    public void reset(){
        this.parameters.clear();
    }
}
