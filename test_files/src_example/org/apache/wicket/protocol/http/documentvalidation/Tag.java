package org.apache.wicket.protocol.http.documentvalidation;

import java.util.*;

public class Tag implements DocumentElement{
    private final Map<String,String> expectedAttributes;
    private final List<DocumentElement> expectedChildren;
    private final Set<String> illegalAttributes;
    private final String tag;
    public Tag(final String tag){
        super();
        this.expectedAttributes=(Map<String,String>)new HashMap();
        this.expectedChildren=(List<DocumentElement>)new ArrayList();
        this.illegalAttributes=(Set<String>)new HashSet();
        this.tag=tag.toLowerCase();
    }
    public void addExpectedAttribute(final String name,final String pattern){
        this.expectedAttributes.put(name.toLowerCase(),pattern);
    }
    public Tag addExpectedChild(final DocumentElement e){
        this.expectedChildren.add(e);
        return this;
    }
    public void addIllegalAttribute(final String name){
        this.illegalAttributes.add(name.toLowerCase());
    }
    public Map<String,String> getExpectedAttributes(){
        return this.expectedAttributes;
    }
    public List<DocumentElement> getExpectedChildren(){
        return this.expectedChildren;
    }
    public Set<String> getIllegalAttributes(){
        return this.illegalAttributes;
    }
    public String getTag(){
        return this.tag;
    }
    public String toString(){
        return "[tag = '"+this.tag+"']";
    }
}
