package org.apache.commons.lang.math;

import java.io.Serializable;
import org.apache.commons.lang.math.Range;

public final class FloatRange extends Range implements Serializable{
    private static final long serialVersionUID=71849363892750L;
    private final float min;
    private final float max;
    private transient Float minObject;
    private transient Float maxObject;
    private transient int hashCode;
    private transient String toString;
    public FloatRange(final float number){
        super();
        this.minObject=null;
        this.maxObject=null;
        this.hashCode=0;
        this.toString=null;
        if(Float.isNaN(number)){
            throw new IllegalArgumentException("The number must not be NaN");
        }
        this.min=number;
        this.max=number;
    }
    public FloatRange(final Number number){
        super();
        this.minObject=null;
        this.maxObject=null;
        this.hashCode=0;
        this.toString=null;
        if(number==null){
            throw new IllegalArgumentException("The number must not be null");
        }
        this.min=number.floatValue();
        this.max=number.floatValue();
        if(Float.isNaN(this.min)||Float.isNaN(this.max)){
            throw new IllegalArgumentException("The number must not be NaN");
        }
        if(number instanceof Float){
            this.minObject=(Float)number;
            this.maxObject=(Float)number;
        }
    }
    public FloatRange(final float number1,final float number2){
        super();
        this.minObject=null;
        this.maxObject=null;
        this.hashCode=0;
        this.toString=null;
        if(Float.isNaN(number1)||Float.isNaN(number2)){
            throw new IllegalArgumentException("The numbers must not be NaN");
        }
        if(number2<number1){
            this.min=number2;
            this.max=number1;
        }
        else{
            this.min=number1;
            this.max=number2;
        }
    }
    public FloatRange(final Number number1,final Number number2){
        super();
        this.minObject=null;
        this.maxObject=null;
        this.hashCode=0;
        this.toString=null;
        if(number1==null||number2==null){
            throw new IllegalArgumentException("The numbers must not be null");
        }
        final float number1val=number1.floatValue();
        final float number2val=number2.floatValue();
        if(Float.isNaN(number1val)||Float.isNaN(number2val)){
            throw new IllegalArgumentException("The numbers must not be NaN");
        }
        if(number2val<number1val){
            this.min=number2val;
            this.max=number1val;
            if(number2 instanceof Float){
                this.minObject=(Float)number2;
            }
            if(number1 instanceof Float){
                this.maxObject=(Float)number1;
            }
        }
        else{
            this.min=number1val;
            this.max=number2val;
            if(number1 instanceof Float){
                this.minObject=(Float)number1;
            }
            if(number2 instanceof Float){
                this.maxObject=(Float)number2;
            }
        }
    }
    public Number getMinimumNumber(){
        if(this.minObject==null){
            this.minObject=new Float(this.min);
        }
        return this.minObject;
    }
    public long getMinimumLong(){
        return (long)this.min;
    }
    public int getMinimumInteger(){
        return (int)this.min;
    }
    public double getMinimumDouble(){
        return this.min;
    }
    public float getMinimumFloat(){
        return this.min;
    }
    public Number getMaximumNumber(){
        if(this.maxObject==null){
            this.maxObject=new Float(this.max);
        }
        return this.maxObject;
    }
    public long getMaximumLong(){
        return (long)this.max;
    }
    public int getMaximumInteger(){
        return (int)this.max;
    }
    public double getMaximumDouble(){
        return this.max;
    }
    public float getMaximumFloat(){
        return this.max;
    }
    public boolean containsNumber(final Number number){
        return number!=null&&this.containsFloat(number.floatValue());
    }
    public boolean containsFloat(final float value){
        return value>=this.min&&value<=this.max;
    }
    public boolean containsRange(final Range range){
        return range!=null&&this.containsFloat(range.getMinimumFloat())&&this.containsFloat(range.getMaximumFloat());
    }
    public boolean overlapsRange(final Range range){
        return range!=null&&(range.containsFloat(this.min)||range.containsFloat(this.max)||this.containsFloat(range.getMinimumFloat()));
    }
    public boolean equals(final Object obj){
        if(obj==this){
            return true;
        }
        if(!(obj instanceof FloatRange)){
            return false;
        }
        final FloatRange range=(FloatRange)obj;
        return Float.floatToIntBits(this.min)==Float.floatToIntBits(range.min)&&Float.floatToIntBits(this.max)==Float.floatToIntBits(range.max);
    }
    public int hashCode(){
        if(this.hashCode==0){
            this.hashCode=17;
            this.hashCode=37*this.hashCode+this.getClass().hashCode();
            this.hashCode=37*this.hashCode+Float.floatToIntBits(this.min);
            this.hashCode=37*this.hashCode+Float.floatToIntBits(this.max);
        }
        return this.hashCode;
    }
    public String toString(){
        if(this.toString==null){
            final StringBuffer buf=new StringBuffer(32);
            buf.append("Range[");
            buf.append(this.min);
            buf.append(',');
            buf.append(this.max);
            buf.append(']');
            this.toString=buf.toString();
        }
        return this.toString;
    }
}
