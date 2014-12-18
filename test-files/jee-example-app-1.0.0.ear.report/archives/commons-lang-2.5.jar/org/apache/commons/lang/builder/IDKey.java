package org.apache.commons.lang.builder;

final class IDKey{
    private final Object value;
    private final int id;
    public IDKey(final Object _value){
        super();
        this.id=System.identityHashCode(_value);
        this.value=_value;
    }
    public int hashCode(){
        return this.id;
    }
    public boolean equals(final Object other){
        if(!(other instanceof IDKey)){
            return false;
        }
        final IDKey idKey=(IDKey)other;
        return this.id==idKey.id&&this.value==idKey.value;
    }
}
