package org.apache.commons.lang.mutable;

import org.apache.commons.lang.mutable.Mutable;

public class MutableLong extends Number implements Comparable,Mutable{
    private static final long serialVersionUID=62986528375L;
    private long value;
    public MutableLong(){
        super();
    }
    public MutableLong(final long value){
        super();
        this.value=value;
    }
    public MutableLong(final Number value){
        super();
        this.value=value.longValue();
    }
    public MutableLong(final String value) throws NumberFormatException{
        super();
        this.value=Long.parseLong(value);
    }
    public Object getValue(){
        return new Long(this.value);
    }
    public void setValue(final long value){
        this.value=value;
    }
    public void setValue(final Object value){
        this.setValue(((Number)value).longValue());
    }
    public void increment(){
        ++this.value;
    }
    public void decrement(){
        --this.value;
    }
    public void add(final long operand){
        this.value+=operand;
    }
    public void add(final Number operand){
        this.value+=operand.longValue();
    }
    public void subtract(final long operand){
        this.value-=operand;
    }
    public void subtract(final Number operand){
        this.value-=operand.longValue();
    }
    public int intValue(){
        return (int)this.value;
    }
    public long longValue(){
        return this.value;
    }
    public float floatValue(){
        return this.value;
    }
    public double doubleValue(){
        return this.value;
    }
    public Long toLong(){
        return new Long(this.longValue());
    }
    public boolean equals(final Object obj){
        return obj instanceof MutableLong&&this.value==((MutableLong)obj).longValue();
    }
    public int hashCode(){
        return (int)(this.value^this.value>>>32);
    }
    public int compareTo(final Object obj){
        final MutableLong other=(MutableLong)obj;
        final long anotherVal=other.value;
        return (this.value<anotherVal)?-1:((this.value==anotherVal)?0:1);
    }
    public String toString(){
        return String.valueOf(this.value);
    }
}
