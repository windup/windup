package org.apache.log4j.jmx;

class MethodUnion{
    Method readMethod;
    Method writeMethod;
    MethodUnion(final Method readMethod,final Method writeMethod){
        super();
        this.readMethod=readMethod;
        this.writeMethod=writeMethod;
    }
}
