package org.jboss.windup.rules.apps.java.blacklist;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;

public class Types implements Iterable<ClassCandidateType>
{
    Set<ClassCandidateType> types = new HashSet<ClassCandidateType>();
    
    public static Types add(ClassCandidateType type) {
        Types instance=new Types();
        instance.and(type); // not sure if this looks good (add cannot be called here)
        return instance;
    }
    
    public Types and(ClassCandidateType type){
        types.add(type);
        return this;
    }
    
    public boolean contains(ClassCandidateType type) {
        return types.contains(type);
    }

    @Override
    public Iterator<ClassCandidateType> iterator()
    {
        return types.iterator();
    }
}
