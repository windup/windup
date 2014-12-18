package org.apache.commons.lang.mutable;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.mutable.Mutable;

public class MutableFloat extends Number implements Comparable,Mutable{
    private static final long serialVersionUID=5787169186L;
    private float value;
    public MutableFloat(){
        super();
    }
    public MutableFloat(final float value){
        super();
        this.value=value;
    }
    public MutableFloat(final Number value){
        super();
        this.value=value.floatValue();
    }
    public MutableFloat(final String value) throws NumberFormatException{
        super();
        this.value=Float.parseFloat(value);
    }
    public Object getValue(){
        return new Float(this.value);
    }
    public void setValue(final float value){
        this.value=value;
    }
    public void setValue(final Object value){
        this.setValue(((Number)value).floatValue());
    }
    public boolean isNaN(){
        return Float.isNaN(this.value);
    }
    public boolean isInfinite(){
        return Float.isInfinite(this.value);
    }
    public void increment(){
        ++this.value;
    }
    public void decrement(){
        --this.value;
    }
    public void add(final float operand){
        this.value+=operand;
    }
    public void add(final Number operand){
        this.value+=operand.floatValue();
    }
    public void subtract(final float operand){
        this.value-=operand;
    }
    public void subtract(final Number operand){
        this.value-=operand.floatValue();
    }
    public int intValue(){
        return (int)this.value;
    }
    public long longValue(){
        return (long)this.value;
    }
    public float floatValue(){
        return this.value;
    }
    public double doubleValue(){
        return this.value;
    }
    public Float toFloat(){
        return new Float(this.floatValue());
    }
    public boolean equals(final Object obj){
        return obj instanceof MutableFloat&&Float.floatToIntBits(((MutableFloat)obj).value)==Float.floatToIntBits(this.value);
    }
    public int hashCode(){
        return Float.floatToIntBits(this.value);
    }
    public int compareTo(final Object obj){
        final MutableFloat other=(MutableFloat)obj;
        final float anotherVal=other.value;
        return NumberUtils.compare(this.value,anotherVal);
    }
    public String toString(){
        return String.valueOf(this.value);
    }
}
