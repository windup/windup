package org.apache.commons.lang.math;

import java.io.Serializable;
import org.apache.commons.lang.math.Range;

public final class IntRange extends Range implements Serializable{
    private static final long serialVersionUID=71849363892730L;
    private final int min;
    private final int max;
    private transient Integer minObject;
    private transient Integer maxObject;
    private transient int hashCode;
    private transient String toString;
    public IntRange(final int number){
        super();
        this.minObject=null;
        this.maxObject=null;
        this.hashCode=0;
        this.toString=null;
        this.min=number;
        this.max=number;
    }
    public IntRange(final Number number){
        super();
        this.minObject=null;
        this.maxObject=null;
        this.hashCode=0;
        this.toString=null;
        if(number==null){
            throw new IllegalArgumentException("The number must not be null");
        }
        this.min=number.intValue();
        this.max=number.intValue();
        if(number instanceof Integer){
            this.minObject=(Integer)number;
            this.maxObject=(Integer)number;
        }
    }
    public IntRange(final int number1,final int number2){
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
    public IntRange(final Number number1,final Number number2){
        super();
        this.minObject=null;
        this.maxObject=null;
        this.hashCode=0;
        this.toString=null;
        if(number1==null||number2==null){
            throw new IllegalArgumentException("The numbers must not be null");
        }
        final int number1val=number1.intValue();
        final int number2val=number2.intValue();
        if(number2val<number1val){
            this.min=number2val;
            this.max=number1val;
            if(number2 instanceof Integer){
                this.minObject=(Integer)number2;
            }
            if(number1 instanceof Integer){
                this.maxObject=(Integer)number1;
            }
        }
        else{
            this.min=number1val;
            this.max=number2val;
            if(number1 instanceof Integer){
                this.minObject=(Integer)number1;
            }
            if(number2 instanceof Integer){
                this.maxObject=(Integer)number2;
            }
        }
    }
    public Number getMinimumNumber(){
        if(this.minObject==null){
            this.minObject=new Integer(this.min);
        }
        return this.minObject;
    }
    public long getMinimumLong(){
        return this.min;
    }
    public int getMinimumInteger(){
        return this.min;
    }
    public double getMinimumDouble(){
        return this.min;
    }
    public float getMinimumFloat(){
        return this.min;
    }
    public Number getMaximumNumber(){
        if(this.maxObject==null){
            this.maxObject=new Integer(this.max);
        }
        return this.maxObject;
    }
    public long getMaximumLong(){
        return this.max;
    }
    public int getMaximumInteger(){
        return this.max;
    }
    public double getMaximumDouble(){
        return this.max;
    }
    public float getMaximumFloat(){
        return this.max;
    }
    public boolean containsNumber(final Number number){
        return number!=null&&this.containsInteger(number.intValue());
    }
    public boolean containsInteger(final int value){
        return value>=this.min&&value<=this.max;
    }
    public boolean containsRange(final Range range){
        return range!=null&&this.containsInteger(range.getMinimumInteger())&&this.containsInteger(range.getMaximumInteger());
    }
    public boolean overlapsRange(final Range range){
        return range!=null&&(range.containsInteger(this.min)||range.containsInteger(this.max)||this.containsInteger(range.getMinimumInteger()));
    }
    public boolean equals(final Object obj){
        if(obj==this){
            return true;
        }
        if(!(obj instanceof IntRange)){
            return false;
        }
        final IntRange range=(IntRange)obj;
        return this.min==range.min&&this.max==range.max;
    }
    public int hashCode(){
        if(this.hashCode==0){
            this.hashCode=17;
            this.hashCode=37*this.hashCode+this.getClass().hashCode();
            this.hashCode=37*this.hashCode+this.min;
            this.hashCode=37*this.hashCode+this.max;
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
    public int[] toArray(){
        final int[] array=new int[this.max-this.min+1];
        for(int i=0;i<array.length;++i){
            array[i]=this.min+i;
        }
        return array;
    }
}
