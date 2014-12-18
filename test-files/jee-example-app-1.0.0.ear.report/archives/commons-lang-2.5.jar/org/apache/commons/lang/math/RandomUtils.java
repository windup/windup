package org.apache.commons.lang.math;

import org.apache.commons.lang.math.JVMRandom;
import java.util.Random;

public class RandomUtils{
    public static final Random JVM_RANDOM;
    public static int nextInt(){
        return nextInt(RandomUtils.JVM_RANDOM);
    }
    public static int nextInt(final Random random){
        return random.nextInt();
    }
    public static int nextInt(final int n){
        return nextInt(RandomUtils.JVM_RANDOM,n);
    }
    public static int nextInt(final Random random,final int n){
        return random.nextInt(n);
    }
    public static long nextLong(){
        return nextLong(RandomUtils.JVM_RANDOM);
    }
    public static long nextLong(final Random random){
        return random.nextLong();
    }
    public static boolean nextBoolean(){
        return nextBoolean(RandomUtils.JVM_RANDOM);
    }
    public static boolean nextBoolean(final Random random){
        return random.nextBoolean();
    }
    public static float nextFloat(){
        return nextFloat(RandomUtils.JVM_RANDOM);
    }
    public static float nextFloat(final Random random){
        return random.nextFloat();
    }
    public static double nextDouble(){
        return nextDouble(RandomUtils.JVM_RANDOM);
    }
    public static double nextDouble(final Random random){
        return random.nextDouble();
    }
    static{
        JVM_RANDOM=new JVMRandom();
    }
}
