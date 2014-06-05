package org.apache.wicket.util.resource.locator;

import java.util.*;

public final class EmptyResourceNameIterator extends ResourceNameIterator{
    public EmptyResourceNameIterator(){
        super(null,null,null,null,null,true);
    }
    public boolean hasNext(){
        return false;
    }
    public void remove(){
    }
}
