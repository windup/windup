package org.apache.commons.lang.mutable;

import org.apache.commons.lang.mutable.Mutable;

public class MutableShort extends Number implements Comparable,Mutable{
    private static final long serialVersionUID=-2135791679L;
    private short value;
    public MutableShort(){
        super();
    }
    public MutableShort(final short value){
        super();
        this.value=value;
    }
    public MutableShort(final Number value){
        super();
        this.value=value.shortValue();
    }
    public MutableShort(final String value) throws NumberFormatException{
        super();
        this.value=Short.parseShort(value);
    }
    public Object getValue(){
        return new Short(this.value);
    }
    public void setValue(final short value){
        this.value=value;
    }
    public void setValue(final Object value){
        this.setValue(((Number)value).shortValue());
    }
    public void increment(){
        ++this.value;
    }
    public void decrement(){
        --this.value;
    }
    public void add(final short operand){
        this.value+=operand;
    }
    public void add(final Number operand){
        this.value+=operand.shortValue();
    }
    public void subtract(final short operand){
        this.value-=operand;
    }
    public void subtract(final Number operand){
        this.value-=operand.shortValue();
    }
    public short shortValue(){
        return this.value;
    }
    public int intValue(){
        return this.value;
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
    public Short toShort(){
        return new Short(this.shortValue());
    }
    public boolean equals(final Object obj){
        return obj instanceof MutableShort&&this.value==((MutableShort)obj).shortValue();
    }
    public int hashCode(){
        return this.value;
    }
    public int compareTo(final Object obj){
        final MutableShort other=(MutableShort)obj;
        final short anotherVal=other.value;
        return (this.value<anotherVal)?-1:((this.value==anotherVal)?0:1);
    }
    public String toString(){
        return String.valueOf(this.value);
    }
}
