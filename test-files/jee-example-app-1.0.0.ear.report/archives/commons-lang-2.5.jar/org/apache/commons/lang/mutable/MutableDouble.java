package org.apache.commons.lang.mutable;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.mutable.Mutable;

public class MutableDouble extends Number implements Comparable,Mutable{
    private static final long serialVersionUID=1587163916L;
    private double value;
    public MutableDouble(){
        super();
    }
    public MutableDouble(final double value){
        super();
        this.value=value;
    }
    public MutableDouble(final Number value){
        super();
        this.value=value.doubleValue();
    }
    public MutableDouble(final String value) throws NumberFormatException{
        super();
        this.value=Double.parseDouble(value);
    }
    public Object getValue(){
        return new Double(this.value);
    }
    public void setValue(final double value){
        this.value=value;
    }
    public void setValue(final Object value){
        this.setValue(((Number)value).doubleValue());
    }
    public boolean isNaN(){
        return Double.isNaN(this.value);
    }
    public boolean isInfinite(){
        return Double.isInfinite(this.value);
    }
    public void increment(){
        ++this.value;
    }
    public void decrement(){
        --this.value;
    }
    public void add(final double operand){
        this.value+=operand;
    }
    public void add(final Number operand){
        this.value+=operand.doubleValue();
    }
    public void subtract(final double operand){
        this.value-=operand;
    }
    public void subtract(final Number operand){
        this.value-=operand.doubleValue();
    }
    public int intValue(){
        return (int)this.value;
    }
    public long longValue(){
        return (long)this.value;
    }
    public float floatValue(){
        return (float)this.value;
    }
    public double doubleValue(){
        return this.value;
    }
    public Double toDouble(){
        return new Double(this.doubleValue());
    }
    public boolean equals(final Object obj){
        return obj instanceof MutableDouble&&Double.doubleToLongBits(((MutableDouble)obj).value)==Double.doubleToLongBits(this.value);
    }
    public int hashCode(){
        final long bits=Double.doubleToLongBits(this.value);
        return (int)(bits^bits>>>32);
    }
    public int compareTo(final Object obj){
        final MutableDouble other=(MutableDouble)obj;
        final double anotherVal=other.value;
        return NumberUtils.compare(this.value,anotherVal);
    }
    public String toString(){
        return String.valueOf(this.value);
    }
}
