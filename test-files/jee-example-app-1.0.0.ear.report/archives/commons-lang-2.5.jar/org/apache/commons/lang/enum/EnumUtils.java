package org.apache.commons.lang.enum;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.enum.ValuedEnum;
import org.apache.commons.lang.enum.Enum;

public class EnumUtils{
    public static Enum getEnum(final Class enumClass,final String name){
        return Enum.getEnum(enumClass,name);
    }
    public static ValuedEnum getEnum(final Class enumClass,final int value){
        return (ValuedEnum)ValuedEnum.getEnum(enumClass,value);
    }
    public static Map getEnumMap(final Class enumClass){
        return Enum.getEnumMap(enumClass);
    }
    public static List getEnumList(final Class enumClass){
        return Enum.getEnumList(enumClass);
    }
    public static Iterator iterator(final Class enumClass){
        return Enum.getEnumList(enumClass).iterator();
    }
}
