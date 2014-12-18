package org.apache.commons.lang.math;

import java.math.BigInteger;

public final class Fraction extends Number implements Comparable{
    private static final long serialVersionUID=65382027393090L;
    public static final Fraction ZERO;
    public static final Fraction ONE;
    public static final Fraction ONE_HALF;
    public static final Fraction ONE_THIRD;
    public static final Fraction TWO_THIRDS;
    public static final Fraction ONE_QUARTER;
    public static final Fraction TWO_QUARTERS;
    public static final Fraction THREE_QUARTERS;
    public static final Fraction ONE_FIFTH;
    public static final Fraction TWO_FIFTHS;
    public static final Fraction THREE_FIFTHS;
    public static final Fraction FOUR_FIFTHS;
    private final int numerator;
    private final int denominator;
    private transient int hashCode;
    private transient String toString;
    private transient String toProperString;
    private Fraction(final int numerator,final int denominator){
        super();
        this.hashCode=0;
        this.toString=null;
        this.toProperString=null;
        this.numerator=numerator;
        this.denominator=denominator;
    }
    public static Fraction getFraction(int numerator,int denominator){
        if(denominator==0){
            throw new ArithmeticException("The denominator must not be zero");
        }
        if(denominator<0){
            if(numerator==Integer.MIN_VALUE||denominator==Integer.MIN_VALUE){
                throw new ArithmeticException("overflow: can't negate");
            }
            numerator=-numerator;
            denominator=-denominator;
        }
        return new Fraction(numerator,denominator);
    }
    public static Fraction getFraction(final int whole,final int numerator,final int denominator){
        if(denominator==0){
            throw new ArithmeticException("The denominator must not be zero");
        }
        if(denominator<0){
            throw new ArithmeticException("The denominator must not be negative");
        }
        if(numerator<0){
            throw new ArithmeticException("The numerator must not be negative");
        }
        long numeratorValue;
        if(whole<0){
            numeratorValue=whole*denominator-numerator;
        }
        else{
            numeratorValue=whole*denominator+numerator;
        }
        if(numeratorValue<-2147483648L||numeratorValue>2147483647L){
            throw new ArithmeticException("Numerator too large to represent as an Integer.");
        }
        return new Fraction((int)numeratorValue,denominator);
    }
    public static Fraction getReducedFraction(int numerator,int denominator){
        if(denominator==0){
            throw new ArithmeticException("The denominator must not be zero");
        }
        if(numerator==0){
            return Fraction.ZERO;
        }
        if(denominator==Integer.MIN_VALUE&&(numerator&0x1)==0x0){
            numerator/=2;
            denominator/=2;
        }
        if(denominator<0){
            if(numerator==Integer.MIN_VALUE||denominator==Integer.MIN_VALUE){
                throw new ArithmeticException("overflow: can't negate");
            }
            numerator=-numerator;
            denominator=-denominator;
        }
        final int gcd=greatestCommonDivisor(numerator,denominator);
        numerator/=gcd;
        denominator/=gcd;
        return new Fraction(numerator,denominator);
    }
    public static Fraction getFraction(double value){
        final int sign=(value<0.0)?-1:1;
        value=Math.abs(value);
        if(value>2.147483647E9||Double.isNaN(value)){
            throw new ArithmeticException("The value must not be greater than Integer.MAX_VALUE or NaN");
        }
        final int wholeNumber=(int)value;
        value-=wholeNumber;
        int numer0=0;
        int denom0=1;
        int numer=1;
        int denom=0;
        int numer2=0;
        int denom2=0;
        int a1=(int)value;
        int a2=0;
        double x1=1.0;
        double x2=0.0;
        double y1=value-a1;
        double y2=0.0;
        double delta2=Double.MAX_VALUE;
        int i=1;
        do{
            delta2=delta2;
            a2=(int)(x1/y1);
            x2=y1;
            y2=x1-a2*y1;
            numer2=a1*numer+numer0;
            denom2=a1*denom+denom0;
            final double fraction=numer2/denom2;
            delta2=Math.abs(value-fraction);
            a1=a2;
            x1=x2;
            y1=y2;
            numer0=numer;
            denom0=denom;
            numer=numer2;
            denom=denom2;
            ++i;
        } while(delta2>delta2&&denom2<=10000&&denom2>0&&i<25);
        if(i==25){
            throw new ArithmeticException("Unable to convert double to fraction");
        }
        return getReducedFraction((numer0+wholeNumber*denom0)*sign,denom0);
    }
    public static Fraction getFraction(String str){
        if(str==null){
            throw new IllegalArgumentException("The string must not be null");
        }
        int pos=str.indexOf(46);
        if(pos>=0){
            return getFraction(Double.parseDouble(str));
        }
        pos=str.indexOf(32);
        if(pos>0){
            final int whole=Integer.parseInt(str.substring(0,pos));
            str=str.substring(pos+1);
            pos=str.indexOf(47);
            if(pos<0){
                throw new NumberFormatException("The fraction could not be parsed as the format X Y/Z");
            }
            final int numer=Integer.parseInt(str.substring(0,pos));
            final int denom=Integer.parseInt(str.substring(pos+1));
            return getFraction(whole,numer,denom);
        }
        else{
            pos=str.indexOf(47);
            if(pos<0){
                return getFraction(Integer.parseInt(str),1);
            }
            final int numer2=Integer.parseInt(str.substring(0,pos));
            final int denom2=Integer.parseInt(str.substring(pos+1));
            return getFraction(numer2,denom2);
        }
    }
    public int getNumerator(){
        return this.numerator;
    }
    public int getDenominator(){
        return this.denominator;
    }
    public int getProperNumerator(){
        return Math.abs(this.numerator%this.denominator);
    }
    public int getProperWhole(){
        return this.numerator/this.denominator;
    }
    public int intValue(){
        return this.numerator/this.denominator;
    }
    public long longValue(){
        return this.numerator/this.denominator;
    }
    public float floatValue(){
        return this.numerator/this.denominator;
    }
    public double doubleValue(){
        return this.numerator/this.denominator;
    }
    public Fraction reduce(){
        if(this.numerator==0){
            return this.equals(Fraction.ZERO)?this:Fraction.ZERO;
        }
        final int gcd=greatestCommonDivisor(Math.abs(this.numerator),this.denominator);
        if(gcd==1){
            return this;
        }
        return getFraction(this.numerator/gcd,this.denominator/gcd);
    }
    public Fraction invert(){
        if(this.numerator==0){
            throw new ArithmeticException("Unable to invert zero.");
        }
        if(this.numerator==Integer.MIN_VALUE){
            throw new ArithmeticException("overflow: can't negate numerator");
        }
        if(this.numerator<0){
            return new Fraction(-this.denominator,-this.numerator);
        }
        return new Fraction(this.denominator,this.numerator);
    }
    public Fraction negate(){
        if(this.numerator==Integer.MIN_VALUE){
            throw new ArithmeticException("overflow: too large to negate");
        }
        return new Fraction(-this.numerator,this.denominator);
    }
    public Fraction abs(){
        if(this.numerator>=0){
            return this;
        }
        return this.negate();
    }
    public Fraction pow(final int power){
        if(power==1){
            return this;
        }
        if(power==0){
            return Fraction.ONE;
        }
        if(power<0){
            if(power==Integer.MIN_VALUE){
                return this.invert().pow(2).pow(-(power/2));
            }
            return this.invert().pow(-power);
        }
        else{
            final Fraction f=this.multiplyBy(this);
            if(power%2==0){
                return f.pow(power/2);
            }
            return f.pow(power/2).multiplyBy(this);
        }
    }
    private static int greatestCommonDivisor(int u,int v){
        if(Math.abs(u)<=1||Math.abs(v)<=1){
            return 1;
        }
        if(u>0){
            u=-u;
        }
        if(v>0){
            v=-v;
        }
        int k;
        for(k=0;(u&0x1)==0x0&&(v&0x1)==0x0&&k<31;u/=2,v/=2,++k){
        }
        if(k==31){
            throw new ArithmeticException("overflow: gcd is 2^31");
        }
        int t=((u&0x1)==0x1)?v:(-(u/2));
        while(true){
            if((t&0x1)==0x0){
                t/=2;
            }
            else{
                if(t>0){
                    u=-t;
                }
                else{
                    v=t;
                }
                t=(v-u)/2;
                if(t==0){
                    break;
                }
                continue;
            }
        }
        return -u*(1<<k);
    }
    private static int mulAndCheck(final int x,final int y){
        final long m=x*y;
        if(m<-2147483648L||m>2147483647L){
            throw new ArithmeticException("overflow: mul");
        }
        return (int)m;
    }
    private static int mulPosAndCheck(final int x,final int y){
        final long m=x*y;
        if(m>2147483647L){
            throw new ArithmeticException("overflow: mulPos");
        }
        return (int)m;
    }
    private static int addAndCheck(final int x,final int y){
        final long s=x+y;
        if(s<-2147483648L||s>2147483647L){
            throw new ArithmeticException("overflow: add");
        }
        return (int)s;
    }
    private static int subAndCheck(final int x,final int y){
        final long s=x-y;
        if(s<-2147483648L||s>2147483647L){
            throw new ArithmeticException("overflow: add");
        }
        return (int)s;
    }
    public Fraction add(final Fraction fraction){
        return this.addSub(fraction,true);
    }
    public Fraction subtract(final Fraction fraction){
        return this.addSub(fraction,false);
    }
    private Fraction addSub(final Fraction fraction,final boolean isAdd){
        if(fraction==null){
            throw new IllegalArgumentException("The fraction must not be null");
        }
        if(this.numerator==0){
            return isAdd?fraction:fraction.negate();
        }
        if(fraction.numerator==0){
            return this;
        }
        final int d1=greatestCommonDivisor(this.denominator,fraction.denominator);
        if(d1==1){
            final int uvp=mulAndCheck(this.numerator,fraction.denominator);
            final int upv=mulAndCheck(fraction.numerator,this.denominator);
            return new Fraction(isAdd?addAndCheck(uvp,upv):subAndCheck(uvp,upv),mulPosAndCheck(this.denominator,fraction.denominator));
        }
        final BigInteger uvp2=BigInteger.valueOf(this.numerator).multiply(BigInteger.valueOf(fraction.denominator/d1));
        final BigInteger upv2=BigInteger.valueOf(fraction.numerator).multiply(BigInteger.valueOf(this.denominator/d1));
        final BigInteger t=isAdd?uvp2.add(upv2):uvp2.subtract(upv2);
        final int tmodd1=t.mod(BigInteger.valueOf(d1)).intValue();
        final int d2=(tmodd1==0)?d1:greatestCommonDivisor(tmodd1,d1);
        final BigInteger w=t.divide(BigInteger.valueOf(d2));
        if(w.bitLength()>31){
            throw new ArithmeticException("overflow: numerator too large after multiply");
        }
        return new Fraction(w.intValue(),mulPosAndCheck(this.denominator/d1,fraction.denominator/d2));
    }
    public Fraction multiplyBy(final Fraction fraction){
        if(fraction==null){
            throw new IllegalArgumentException("The fraction must not be null");
        }
        if(this.numerator==0||fraction.numerator==0){
            return Fraction.ZERO;
        }
        final int d1=greatestCommonDivisor(this.numerator,fraction.denominator);
        final int d2=greatestCommonDivisor(fraction.numerator,this.denominator);
        return getReducedFraction(mulAndCheck(this.numerator/d1,fraction.numerator/d2),mulPosAndCheck(this.denominator/d2,fraction.denominator/d1));
    }
    public Fraction divideBy(final Fraction fraction){
        if(fraction==null){
            throw new IllegalArgumentException("The fraction must not be null");
        }
        if(fraction.numerator==0){
            throw new ArithmeticException("The fraction to divide by must not be zero");
        }
        return this.multiplyBy(fraction.invert());
    }
    public boolean equals(final Object obj){
        if(obj==this){
            return true;
        }
        if(!(obj instanceof Fraction)){
            return false;
        }
        final Fraction other=(Fraction)obj;
        return this.getNumerator()==other.getNumerator()&&this.getDenominator()==other.getDenominator();
    }
    public int hashCode(){
        if(this.hashCode==0){
            this.hashCode=37*(629+this.getNumerator())+this.getDenominator();
        }
        return this.hashCode;
    }
    public int compareTo(final Object object){
        final Fraction other=(Fraction)object;
        if(this==other){
            return 0;
        }
        if(this.numerator==other.numerator&&this.denominator==other.denominator){
            return 0;
        }
        final long first=this.numerator*other.denominator;
        final long second=other.numerator*this.denominator;
        if(first==second){
            return 0;
        }
        if(first<second){
            return -1;
        }
        return 1;
    }
    public String toString(){
        if(this.toString==null){
            this.toString=new StringBuffer(32).append(this.getNumerator()).append('/').append(this.getDenominator()).toString();
        }
        return this.toString;
    }
    public String toProperString(){
        if(this.toProperString==null){
            if(this.numerator==0){
                this.toProperString="0";
            }
            else if(this.numerator==this.denominator){
                this.toProperString="1";
            }
            else if(this.numerator==-1*this.denominator){
                this.toProperString="-1";
            }
            else if(((this.numerator>0)?(-this.numerator):this.numerator)<-this.denominator){
                final int properNumerator=this.getProperNumerator();
                if(properNumerator==0){
                    this.toProperString=Integer.toString(this.getProperWhole());
                }
                else{
                    this.toProperString=new StringBuffer(32).append(this.getProperWhole()).append(' ').append(properNumerator).append('/').append(this.getDenominator()).toString();
                }
            }
            else{
                this.toProperString=new StringBuffer(32).append(this.getNumerator()).append('/').append(this.getDenominator()).toString();
            }
        }
        return this.toProperString;
    }
    static{
        ZERO=new Fraction(0,1);
        ONE=new Fraction(1,1);
        ONE_HALF=new Fraction(1,2);
        ONE_THIRD=new Fraction(1,3);
        TWO_THIRDS=new Fraction(2,3);
        ONE_QUARTER=new Fraction(1,4);
        TWO_QUARTERS=new Fraction(2,4);
        THREE_QUARTERS=new Fraction(3,4);
        ONE_FIFTH=new Fraction(1,5);
        TWO_FIFTHS=new Fraction(2,5);
        THREE_FIFTHS=new Fraction(3,5);
        FOUR_FIFTHS=new Fraction(4,5);
    }
}
