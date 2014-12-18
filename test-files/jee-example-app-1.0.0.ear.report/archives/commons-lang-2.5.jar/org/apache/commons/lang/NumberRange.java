package org.apache.commons.lang;

public final class NumberRange{
    private final Number min;
    private final Number max;
    public NumberRange(final Number num){
        super();
        if(num==null){
            throw new NullPointerException("The number must not be null");
        }
        this.min=num;
        this.max=num;
    }
    public NumberRange(final Number min,final Number max){
        super();
        if(min==null){
            throw new NullPointerException("The minimum value must not be null");
        }
        if(max==null){
            throw new NullPointerException("The maximum value must not be null");
        }
        if(max.doubleValue()<min.doubleValue()){
            this.max=min;
            this.min=min;
        }
        else{
            this.min=min;
            this.max=max;
        }
    }
    public Number getMinimum(){
        return this.min;
    }
    public Number getMaximum(){
        return this.max;
    }
    public boolean includesNumber(final Number number){
        return number!=null&&this.min.doubleValue()<=number.doubleValue()&&this.max.doubleValue()>=number.doubleValue();
    }
    public boolean includesRange(final NumberRange range){
        return range!=null&&this.includesNumber(range.min)&&this.includesNumber(range.max);
    }
    public boolean overlaps(final NumberRange range){
        return range!=null&&(range.includesNumber(this.min)||range.includesNumber(this.max)||this.includesRange(range));
    }
    public boolean equals(final Object obj){
        if(obj==this){
            return true;
        }
        if(!(obj instanceof NumberRange)){
            return false;
        }
        final NumberRange range=(NumberRange)obj;
        return this.min.equals(range.min)&&this.max.equals(range.max);
    }
    public int hashCode(){
        int result=17;
        result=37*result+this.min.hashCode();
        result=37*result+this.max.hashCode();
        return result;
    }
    public String toString(){
        final StringBuffer sb=new StringBuffer();
        if(this.min.doubleValue()<0.0){
            sb.append('(').append(this.min).append(')');
        }
        else{
            sb.append(this.min);
        }
        sb.append('-');
        if(this.max.doubleValue()<0.0){
            sb.append('(').append(this.max).append(')');
        }
        else{
            sb.append(this.max);
        }
        return sb.toString();
    }
}
