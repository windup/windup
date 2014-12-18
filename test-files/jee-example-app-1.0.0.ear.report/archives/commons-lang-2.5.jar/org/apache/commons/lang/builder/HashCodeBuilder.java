package org.apache.commons.lang.builder;

import java.util.HashSet;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import java.util.Collection;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.IDKey;
import java.util.Set;

public class HashCodeBuilder{
    private static final ThreadLocal REGISTRY;
    private final int iConstant;
    private int iTotal;
    static /* synthetic */ Class class$org$apache$commons$lang$builder$HashCodeBuilder;
    static Set getRegistry(){
        return HashCodeBuilder.REGISTRY.get();
    }
    static boolean isRegistered(final Object value){
        final Set registry=getRegistry();
        return registry!=null&&registry.contains(new IDKey(value));
    }
    private static void reflectionAppend(final Object object,final Class clazz,final HashCodeBuilder builder,final boolean useTransients,final String[] excludeFields){
        if(isRegistered(object)){
            return;
        }
        try{
            register(object);
            final Field[] fields=clazz.getDeclaredFields();
            AccessibleObject.setAccessible(fields,true);
            for(int i=0;i<fields.length;++i){
                final Field field=fields[i];
                if(!ArrayUtils.contains(excludeFields,field.getName())&&field.getName().indexOf(36)==-1&&(useTransients||!Modifier.isTransient(field.getModifiers()))&&!Modifier.isStatic(field.getModifiers())){
                    try{
                        final Object fieldValue=field.get(object);
                        builder.append(fieldValue);
                    }
                    catch(IllegalAccessException e){
                        throw new InternalError("Unexpected IllegalAccessException");
                    }
                }
            }
        }
        finally{
            unregister(object);
        }
    }
    public static int reflectionHashCode(final int initialNonZeroOddNumber,final int multiplierNonZeroOddNumber,final Object object){
        return reflectionHashCode(initialNonZeroOddNumber,multiplierNonZeroOddNumber,object,false,null,null);
    }
    public static int reflectionHashCode(final int initialNonZeroOddNumber,final int multiplierNonZeroOddNumber,final Object object,final boolean testTransients){
        return reflectionHashCode(initialNonZeroOddNumber,multiplierNonZeroOddNumber,object,testTransients,null,null);
    }
    public static int reflectionHashCode(final int initialNonZeroOddNumber,final int multiplierNonZeroOddNumber,final Object object,final boolean testTransients,final Class reflectUpToClass){
        return reflectionHashCode(initialNonZeroOddNumber,multiplierNonZeroOddNumber,object,testTransients,reflectUpToClass,null);
    }
    public static int reflectionHashCode(final int initialNonZeroOddNumber,final int multiplierNonZeroOddNumber,final Object object,final boolean testTransients,final Class reflectUpToClass,final String[] excludeFields){
        if(object==null){
            throw new IllegalArgumentException("The object to build a hash code for must not be null");
        }
        final HashCodeBuilder builder=new HashCodeBuilder(initialNonZeroOddNumber,multiplierNonZeroOddNumber);
        Class clazz=object.getClass();
        reflectionAppend(object,clazz,builder,testTransients,excludeFields);
        while(clazz.getSuperclass()!=null&&clazz!=reflectUpToClass){
            clazz=clazz.getSuperclass();
            reflectionAppend(object,clazz,builder,testTransients,excludeFields);
        }
        return builder.toHashCode();
    }
    public static int reflectionHashCode(final Object object){
        return reflectionHashCode(17,37,object,false,null,null);
    }
    public static int reflectionHashCode(final Object object,final boolean testTransients){
        return reflectionHashCode(17,37,object,testTransients,null,null);
    }
    public static int reflectionHashCode(final Object object,final Collection excludeFields){
        return reflectionHashCode(object,ReflectionToStringBuilder.toNoNullStringArray(excludeFields));
    }
    public static int reflectionHashCode(final Object object,final String[] excludeFields){
        return reflectionHashCode(17,37,object,false,null,excludeFields);
    }
    static void register(final Object value){
        Class class$;
        Class class$org$apache$commons$lang$builder$HashCodeBuilder;
        if(HashCodeBuilder.class$org$apache$commons$lang$builder$HashCodeBuilder==null){
            class$org$apache$commons$lang$builder$HashCodeBuilder=(HashCodeBuilder.class$org$apache$commons$lang$builder$HashCodeBuilder=(class$=class$("org.apache.commons.lang.builder.HashCodeBuilder")));
        }
        else{
            class$=(class$org$apache$commons$lang$builder$HashCodeBuilder=HashCodeBuilder.class$org$apache$commons$lang$builder$HashCodeBuilder);
        }
        final Class clazz=class$org$apache$commons$lang$builder$HashCodeBuilder;
        synchronized(class$){
            if(getRegistry()==null){
                HashCodeBuilder.REGISTRY.set(new HashSet());
            }
        }
        getRegistry().add(new IDKey(value));
    }
    static void unregister(final Object value){
        Set registry=getRegistry();
        if(registry!=null){
            registry.remove(new IDKey(value));
            Class class$;
            Class class$org$apache$commons$lang$builder$HashCodeBuilder;
            if(HashCodeBuilder.class$org$apache$commons$lang$builder$HashCodeBuilder==null){
                class$org$apache$commons$lang$builder$HashCodeBuilder=(HashCodeBuilder.class$org$apache$commons$lang$builder$HashCodeBuilder=(class$=class$("org.apache.commons.lang.builder.HashCodeBuilder")));
            }
            else{
                class$=(class$org$apache$commons$lang$builder$HashCodeBuilder=HashCodeBuilder.class$org$apache$commons$lang$builder$HashCodeBuilder);
            }
            final Class clazz=class$org$apache$commons$lang$builder$HashCodeBuilder;
            synchronized(class$){
                registry=getRegistry();
                if(registry!=null&&registry.isEmpty()){
                    HashCodeBuilder.REGISTRY.set(null);
                }
            }
        }
    }
    public HashCodeBuilder(){
        super();
        this.iTotal=0;
        this.iConstant=37;
        this.iTotal=17;
    }
    public HashCodeBuilder(final int initialNonZeroOddNumber,final int multiplierNonZeroOddNumber){
        super();
        this.iTotal=0;
        if(initialNonZeroOddNumber==0){
            throw new IllegalArgumentException("HashCodeBuilder requires a non zero initial value");
        }
        if(initialNonZeroOddNumber%2==0){
            throw new IllegalArgumentException("HashCodeBuilder requires an odd initial value");
        }
        if(multiplierNonZeroOddNumber==0){
            throw new IllegalArgumentException("HashCodeBuilder requires a non zero multiplier");
        }
        if(multiplierNonZeroOddNumber%2==0){
            throw new IllegalArgumentException("HashCodeBuilder requires an odd multiplier");
        }
        this.iConstant=multiplierNonZeroOddNumber;
        this.iTotal=initialNonZeroOddNumber;
    }
    public HashCodeBuilder append(final boolean value){
        this.iTotal=this.iTotal*this.iConstant+(value?0:1);
        return this;
    }
    public HashCodeBuilder append(final boolean[] array){
        if(array==null){
            this.iTotal*=this.iConstant;
        }
        else{
            for(int i=0;i<array.length;++i){
                this.append(array[i]);
            }
        }
        return this;
    }
    public HashCodeBuilder append(final byte value){
        this.iTotal=this.iTotal*this.iConstant+value;
        return this;
    }
    public HashCodeBuilder append(final byte[] array){
        if(array==null){
            this.iTotal*=this.iConstant;
        }
        else{
            for(int i=0;i<array.length;++i){
                this.append(array[i]);
            }
        }
        return this;
    }
    public HashCodeBuilder append(final char value){
        this.iTotal=this.iTotal*this.iConstant+value;
        return this;
    }
    public HashCodeBuilder append(final char[] array){
        if(array==null){
            this.iTotal*=this.iConstant;
        }
        else{
            for(int i=0;i<array.length;++i){
                this.append(array[i]);
            }
        }
        return this;
    }
    public HashCodeBuilder append(final double value){
        return this.append(Double.doubleToLongBits(value));
    }
    public HashCodeBuilder append(final double[] array){
        if(array==null){
            this.iTotal*=this.iConstant;
        }
        else{
            for(int i=0;i<array.length;++i){
                this.append(array[i]);
            }
        }
        return this;
    }
    public HashCodeBuilder append(final float value){
        this.iTotal=this.iTotal*this.iConstant+Float.floatToIntBits(value);
        return this;
    }
    public HashCodeBuilder append(final float[] array){
        if(array==null){
            this.iTotal*=this.iConstant;
        }
        else{
            for(int i=0;i<array.length;++i){
                this.append(array[i]);
            }
        }
        return this;
    }
    public HashCodeBuilder append(final int value){
        this.iTotal=this.iTotal*this.iConstant+value;
        return this;
    }
    public HashCodeBuilder append(final int[] array){
        if(array==null){
            this.iTotal*=this.iConstant;
        }
        else{
            for(int i=0;i<array.length;++i){
                this.append(array[i]);
            }
        }
        return this;
    }
    public HashCodeBuilder append(final long value){
        this.iTotal=this.iTotal*this.iConstant+(int)(value^value>>32);
        return this;
    }
    public HashCodeBuilder append(final long[] array){
        if(array==null){
            this.iTotal*=this.iConstant;
        }
        else{
            for(int i=0;i<array.length;++i){
                this.append(array[i]);
            }
        }
        return this;
    }
    public HashCodeBuilder append(final Object object){
        if(object==null){
            this.iTotal*=this.iConstant;
        }
        else if(object.getClass().isArray()){
            if(object instanceof long[]){
                this.append((long[])object);
            }
            else if(object instanceof int[]){
                this.append((int[])object);
            }
            else if(object instanceof short[]){
                this.append((short[])object);
            }
            else if(object instanceof char[]){
                this.append((char[])object);
            }
            else if(object instanceof byte[]){
                this.append((byte[])object);
            }
            else if(object instanceof double[]){
                this.append((double[])object);
            }
            else if(object instanceof float[]){
                this.append((float[])object);
            }
            else if(object instanceof boolean[]){
                this.append((boolean[])object);
            }
            else{
                this.append((Object[])object);
            }
        }
        else{
            this.iTotal=this.iTotal*this.iConstant+object.hashCode();
        }
        return this;
    }
    public HashCodeBuilder append(final Object[] array){
        if(array==null){
            this.iTotal*=this.iConstant;
        }
        else{
            for(int i=0;i<array.length;++i){
                this.append(array[i]);
            }
        }
        return this;
    }
    public HashCodeBuilder append(final short value){
        this.iTotal=this.iTotal*this.iConstant+value;
        return this;
    }
    public HashCodeBuilder append(final short[] array){
        if(array==null){
            this.iTotal*=this.iConstant;
        }
        else{
            for(int i=0;i<array.length;++i){
                this.append(array[i]);
            }
        }
        return this;
    }
    public HashCodeBuilder appendSuper(final int superHashCode){
        this.iTotal=this.iTotal*this.iConstant+superHashCode;
        return this;
    }
    public int toHashCode(){
        return this.iTotal;
    }
    public int hashCode(){
        return this.toHashCode();
    }
    static /* synthetic */ Class class$(final String x0){
        try{
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x){
            throw new NoClassDefFoundError(x.getMessage());
        }
    }
    static{
        REGISTRY=new ThreadLocal();
    }
}
