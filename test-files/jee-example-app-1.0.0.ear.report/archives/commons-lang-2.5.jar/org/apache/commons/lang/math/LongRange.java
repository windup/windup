package org.apache.commons.lang.math;

import java.io.Serializable;
import org.apache.commons.lang.math.Range;

public final class LongRange extends Range implements Serializable{
    private static final long serialVersionUID=71849363892720L;
    private final long min;
    private final long max;
    private transient Long minObject;
    private transient Long maxObject;
    private transient int hashCode;
    private transient String toString;
    public LongRange(final long number){
        super();
        this.minObject=null;
        this.maxObject=null;
        this.hashCode=0;
        this.toString=null;
        this.min=number;
        this.max=number;
    }
    public LongRange(final Number number){
        super();
        this.minObject=null;
        this.maxObject=null;
        this.hashCode=0;
        this.toString=null;
        if(number==null){
            throw new IllegalArgumentException("The number must not be null");
        }
        this.min=number.longValue();
        this.max=number.longValue();
        if(number instanceof Long){
            this.minObject=(Long)number;
            this.maxObject=(Long)number;
        }
    }
    public LongRange(final long number1,final long number2){
        super();
        this.minObject=null;
        this.maxObject=null;
        this.hashCode=0;
        this.toString=null;
        if(number2<number1){
            this.min=number2;
            this.max=number1;
        }
        else{
            this.min=number1;
            this.max=number2;
        }
    }
    public LongRange(final Number number1,final Number number2){
        super();
        this.minObject=null;
        this.maxObject=null;
        this.hashCode=0;
        this.toString=null;
        if(number1==null||number2==null){
            throw new IllegalArgumentException("The numbers must not be null");
        }
        final long number1val=number1.longValue();
        final long number2val=number2.longValue();
        if(number2val<number1val){
            this.min=number2val;
            this.max=number1val;
            if(number2 instanceof Long){
                this.minObject=(Long)number2;
            }
            if(number1 instanceof Long){
                this.maxObject=(Long)number1;
            }
        }
        else{
            this.min=number1val;
            this.max=number2val;
            if(number1 instanceof Long){
                this.minObject=(Long)number1;
            }
            if(number2 instanceof Long){
                this.maxObject=(Long)number2;
            }
        }
    }
    public Number getMinimumNumber(){
        if(this.minObject==null){
            this.minObject=new Long(this.min);
        }
        return this.minObject;
    }
    public long getMinimumLong(){
        return this.min;
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
            this.maxObject=new Long(this.max);
        }
        return this.maxObject;
    }
    public long getMaximumLong(){
        return this.max;
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
        return number!=null&&this.containsLong(number.longValue());
    }
    public boolean containsLong(final long value){
        return value>=this.min&&value<=this.max;
    }
    public boolean containsRange(final Range range){
        return range!=null&&this.containsLong(range.getMinimumLong())&&this.containsLong(range.getMaximumLong());
    }
    public boolean overlapsRange(final Range range){
        return range!=null&&(range.containsLong(this.min)||range.containsLong(this.max)||this.containsLong(range.getMinimumLong()));
    }
    public boolean equals(final Object obj){
        if(obj==this){
            return true;
        }
        if(!(obj instanceof LongRange)){
            return false;
        }
        final LongRange range=(LongRange)obj;
        return this.min==range.min&&this.max==range.max;
    }
    public int hashCode(){
        if(this.hashCode==0){
            this.hashCode=17;
            this.hashCode=37*this.hashCode+this.getClass().hashCode();
            this.hashCode=37*this.hashCode+(int)(this.min^this.min>>32);
            this.hashCode=37*this.hashCode+(int)(this.max^this.max>>32);
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
    public long[] toArray(){
        final long[] array=new long[(int)(this.max-this.min+1L)];
        for(int i=0;i<array.length;++i){
            array[i]=this.min+i;
        }
        return array;
    }
}
