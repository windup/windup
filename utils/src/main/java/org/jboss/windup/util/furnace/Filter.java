package org.jboss.windup.util.furnace;

public interface Filter<T>
{
    public boolean accept(T name);
}
