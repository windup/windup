package org.apache.commons.lang.mutable;

import org.apache.commons.lang.mutable.Mutable;

public class MutableByte extends Number implements Comparable,Mutable{
    private static final long serialVersionUID=-1585823265L;
    private byte value;
    public MutableByte(){
        super();
    }
    public MutableByte(final byte value){
        super();
        this.value=value;
    }
    public MutableByte(final Number value){
        super();
        this.value=value.byteValue();
    }
    public MutableByte(final String value) throws NumberFormatException{
        super();
        this.value=Byte.parseByte(value);
    }
    public Object getValue(){
        return new Byte(this.value);
    }
    public void setValue(final byte value){
        this.value=value;
    }
    public void setValue(final Object value){
        this.setValue(((Number)value).byteValue());
    }
    public void increment(){
        ++this.value;
    }
    public void decrement(){
        --this.value;
    }
    public void add(final byte operand){
        this.value+=operand;
    }
    public void add(final Number operand){
        this.value+=operand.byteValue();
    }
    public void subtract(final byte operand){
        this.value-=operand;
    }
    public void subtract(final Number operand){
        this.value-=operand.byteValue();
    }
    public byte byteValue(){
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
    public Byte toByte(){
        return new Byte(this.byteValue());
    }
    public boolean equals(final Object obj){
        return obj instanceof MutableByte&&this.value==((MutableByte)obj).byteValue();
    }
    public int hashCode(){
        return this.value;
    }
    public int compareTo(final Object obj){
        final MutableByte other=(MutableByte)obj;
        final byte anotherVal=other.value;
        return (this.value<anotherVal)?-1:((this.value==anotherVal)?0:1);
    }
    public String toString(){
        return String.valueOf(this.value);
    }
}
