package org.apache.commons.lang.enum;

import org.apache.commons.lang.ClassUtils;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.enum.Enum;

public abstract class ValuedEnum extends Enum{
    private static final long serialVersionUID=-7129650521543789085L;
    private final int iValue;
    protected ValuedEnum(final String name,final int value){
        super(name);
        this.iValue=value;
    }
    protected static Enum getEnum(final Class enumClass,final int value){
        if(enumClass==null){
            throw new IllegalArgumentException("The Enum Class must not be null");
        }
        final List list=Enum.getEnumList(enumClass);
        for(final ValuedEnum enumeration : list){
            if(enumeration.getValue()==value){
                return enumeration;
            }
        }
        return null;
    }
    public final int getValue(){
        return this.iValue;
    }
    public int compareTo(final Object other){
        return this.iValue-((ValuedEnum)other).iValue;
    }
    public String toString(){
        if(this.iToString==null){
            final String shortName=ClassUtils.getShortClassName(this.getEnumClass());
            this.iToString=shortName+"["+this.getName()+"="+this.getValue()+"]";
        }
        return this.iToString;
    }
}
