package org.apache.wicket;

public abstract class MetaDataKey<T> implements IClusterable{
    private static final long serialVersionUID=1L;
    public int hashCode(){
        return this.getClass().hashCode();
    }
    public boolean equals(final Object obj){
        return obj!=null&&this.getClass().equals(obj.getClass());
    }
    public T get(final MetaDataEntry<?>[] metaData){
        if(metaData!=null){
            for(final MetaDataEntry<?> entry : metaData){
                if(this.equals(entry.key)){
                    return (T)entry.object;
                }
            }
        }
        return null;
    }
    public MetaDataEntry<?>[] set(MetaDataEntry<?>[] metaData,final Object object){
        boolean set=false;
        if(metaData!=null){
            for(int i=0;i<metaData.length;++i){
                final MetaDataEntry<?> m=metaData[i];
                if(this.equals(m.key)){
                    if(object!=null){
                        m.object=object;
                    }
                    else if(metaData.length>1){
                        final int l=metaData.length-1;
                        final MetaDataEntry<?>[] newMetaData=(MetaDataEntry<?>[])new MetaDataEntry[l];
                        System.arraycopy(metaData,0,newMetaData,0,i);
                        System.arraycopy(metaData,i+1,newMetaData,i,l-i);
                        metaData=newMetaData;
                    }
                    else{
                        metaData=null;
                    }
                    set=true;
                    break;
                }
            }
        }
        if(!set&&object!=null){
            final MetaDataEntry<T> j=new MetaDataEntry<T>(this,object);
            if(metaData==null){
                metaData=(MetaDataEntry<?>[])new MetaDataEntry[] { j };
            }
            else{
                final MetaDataEntry<?>[] newMetaData2=(MetaDataEntry<?>[])new MetaDataEntry[metaData.length+1];
                System.arraycopy(metaData,0,newMetaData2,0,metaData.length);
                newMetaData2[metaData.length]=j;
                metaData=newMetaData2;
            }
        }
        return metaData;
    }
    public String toString(){
        return this.getClass().toString();
    }
}
