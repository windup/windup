package org.apache.wicket.application;

public final class DefaultClassResolver extends AbstractClassResolver{
    public ClassLoader getClassLoader(){
        ClassLoader loader=Thread.currentThread().getContextClassLoader();
        if(loader==null){
            loader=DefaultClassResolver.class.getClassLoader();
        }
        return loader;
    }
}
