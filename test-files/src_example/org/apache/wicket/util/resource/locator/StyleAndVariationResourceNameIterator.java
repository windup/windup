package org.apache.wicket.util.resource.locator;

import java.util.*;

public class StyleAndVariationResourceNameIterator implements Iterator<String>{
    private final String style;
    private final String variation;
    private int state;
    public StyleAndVariationResourceNameIterator(final String style,final String variation){
        super();
        this.state=0;
        this.style=style;
        this.variation=variation;
    }
    public boolean hasNext(){
        return this.state<4;
    }
    public String next(){
        if(this.state==0){
            ++this.state;
            if(this.style!=null&&this.variation!=null){
                return null;
            }
        }
        if(this.state==1){
            ++this.state;
            if(this.style!=null){
                return null;
            }
        }
        if(this.state==2){
            ++this.state;
            if(this.variation!=null){
                return null;
            }
        }
        this.state=4;
        return null;
    }
    public final String getStyle(){
        return (this.state==1||this.state==2)?this.style:null;
    }
    public final String getVariation(){
        return (this.state==1||this.state==3)?this.variation:null;
    }
    public void remove(){
    }
}
