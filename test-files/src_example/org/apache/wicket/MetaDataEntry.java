package org.apache.wicket;

public final class MetaDataEntry<T> implements IClusterable{
    private static final long serialVersionUID=1L;
    final MetaDataKey<T> key;
    Object object;
    public MetaDataEntry(final MetaDataKey<T> key,final Object object){
        super();
        this.key=key;
        this.object=object;
    }
    public String toString(){
        return this.key+"="+this.object.getClass().getName()+"@"+Integer.toHexString(this.object.hashCode());
    }
}
