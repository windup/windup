package org.jboss.windup.rules.apps.java.blacklist;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;

public class Types implements Iterable<TypeReferenceLocation>
{
    Set<TypeReferenceLocation> types = new HashSet<TypeReferenceLocation>();
    
    public static Types add(TypeReferenceLocation type) {
        Types instance=new Types();
        instance.and(type); // not sure if this looks good (add cannot be called here)
        return instance;
    }
    
    public Types and(TypeReferenceLocation type){
        types.add(type);
        return this;
    }
    
    public boolean contains(TypeReferenceLocation type) {
        return types.contains(type);
    }

    @Override
    public Iterator<TypeReferenceLocation> iterator()
    {
        return types.iterator();
    }
}
