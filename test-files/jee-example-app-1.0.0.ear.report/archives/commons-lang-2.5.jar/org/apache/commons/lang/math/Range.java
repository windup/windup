package org.apache.commons.lang.math;

import org.apache.commons.lang.math.NumberUtils;

public abstract class Range{
    public abstract Number getMinimumNumber();
    public long getMinimumLong(){
        return this.getMinimumNumber().longValue();
    }
    public int getMinimumInteger(){
        return this.getMinimumNumber().intValue();
    }
    public double getMinimumDouble(){
        return this.getMinimumNumber().doubleValue();
    }
    public float getMinimumFloat(){
        return this.getMinimumNumber().floatValue();
    }
    public abstract Number getMaximumNumber();
    public long getMaximumLong(){
        return this.getMaximumNumber().longValue();
    }
    public int getMaximumInteger(){
        return this.getMaximumNumber().intValue();
    }
    public double getMaximumDouble(){
        return this.getMaximumNumber().doubleValue();
    }
    public float getMaximumFloat(){
        return this.getMaximumNumber().floatValue();
    }
    public abstract boolean containsNumber(final Number p0);
    public boolean containsLong(final Number value){
        return value!=null&&this.containsLong(value.longValue());
    }
    public boolean containsLong(final long value){
        return value>=this.getMinimumLong()&&value<=this.getMaximumLong();
    }
    public boolean containsInteger(final Number value){
        return value!=null&&this.containsInteger(value.intValue());
    }
    public boolean containsInteger(final int value){
        return value>=this.getMinimumInteger()&&value<=this.getMaximumInteger();
    }
    public boolean containsDouble(final Number value){
        return value!=null&&this.containsDouble(value.doubleValue());
    }
    public boolean containsDouble(final double value){
        final int compareMin=NumberUtils.compare(this.getMinimumDouble(),value);
        final int compareMax=NumberUtils.compare(this.getMaximumDouble(),value);
        return compareMin<=0&&compareMax>=0;
    }
    public boolean containsFloat(final Number value){
        return value!=null&&this.containsFloat(value.floatValue());
    }
    public boolean containsFloat(final float value){
        final int compareMin=NumberUtils.compare(this.getMinimumFloat(),value);
        final int compareMax=NumberUtils.compare(this.getMaximumFloat(),value);
        return compareMin<=0&&compareMax>=0;
    }
    public boolean containsRange(final Range range){
        return range!=null&&this.containsNumber(range.getMinimumNumber())&&this.containsNumber(range.getMaximumNumber());
    }
    public boolean overlapsRange(final Range range){
        return range!=null&&(range.containsNumber(this.getMinimumNumber())||range.containsNumber(this.getMaximumNumber())||this.containsNumber(range.getMinimumNumber()));
    }
    public boolean equals(final Object obj){
        if(obj==this){
            return true;
        }
        if(obj==null||obj.getClass()!=this.getClass()){
            return false;
        }
        final Range range=(Range)obj;
        return this.getMinimumNumber().equals(range.getMinimumNumber())&&this.getMaximumNumber().equals(range.getMaximumNumber());
    }
    public int hashCode(){
        int result=17;
        result=37*result+this.getClass().hashCode();
        result=37*result+this.getMinimumNumber().hashCode();
        result=37*result+this.getMaximumNumber().hashCode();
        return result;
    }
    public String toString(){
        final StringBuffer buf=new StringBuffer(32);
        buf.append("Range[");
        buf.append(this.getMinimumNumber());
        buf.append(',');
        buf.append(this.getMaximumNumber());
        buf.append(']');
        return buf.toString();
    }
}
