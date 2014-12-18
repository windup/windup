package org.apache.log4j;

class CategoryKey{
    String name;
    int hashCache;
    static /* synthetic */ Class class$org$apache$log4j$CategoryKey;
    CategoryKey(final String name){
        super();
        this.name=name.intern();
        this.hashCache=name.hashCode();
    }
    public final int hashCode(){
        return this.hashCache;
    }
    public final boolean equals(final Object rArg){
        return this==rArg||(rArg!=null&&((CategoryKey.class$org$apache$log4j$CategoryKey==null)?(CategoryKey.class$org$apache$log4j$CategoryKey=class$("org.apache.log4j.CategoryKey")):CategoryKey.class$org$apache$log4j$CategoryKey)==rArg.getClass()&&this.name==((CategoryKey)rArg).name);
    }
    static /* synthetic */ Class class$(final String x0){
        try{
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x){
            throw new NoClassDefFoundError(x.getMessage());
        }
    }
}
