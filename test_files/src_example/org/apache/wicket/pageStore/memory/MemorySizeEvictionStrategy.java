package org.apache.wicket.pageStore.memory;

import org.apache.wicket.util.lang.*;
import java.io.*;

public class MemorySizeEvictionStrategy implements DataStoreEvictionStrategy{
    private final Bytes maxBytes;
    public MemorySizeEvictionStrategy(final Bytes maxBytes){
        super();
        Args.notNull((Object)maxBytes,"maxBytes");
        this.maxBytes=maxBytes;
    }
    public void evict(final PageTable pageTable){
        final long storeCurrentSize=WicketObjects.sizeof((Serializable)pageTable);
        if(storeCurrentSize>this.maxBytes.bytes()){
            final PageTableCleaner cleaner=new PageTableCleaner();
            cleaner.drop(pageTable,1);
            this.evict(pageTable);
        }
    }
}
