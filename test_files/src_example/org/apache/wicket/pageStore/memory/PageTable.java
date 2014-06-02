package org.apache.wicket.pageStore.memory;

import org.apache.wicket.*;
import java.util.*;
import java.util.concurrent.*;

class PageTable implements IClusterable{
    private static final long serialVersionUID=1L;
    private final Queue<Integer> index;
    private final ConcurrentMap<Integer,byte[]> pages;
    public PageTable(){
        super();
        this.pages=new ConcurrentHashMap<Integer,byte[]>();
        this.index=new ConcurrentLinkedQueue<Integer>();
    }
    void storePage(final Integer pageId,final byte[] pageAsBytes){
        synchronized(this.index){
            this.pages.put(pageId,pageAsBytes);
            this.updateIndex(pageId);
        }
    }
    byte[] getPage(final Integer pageId){
        synchronized(this.index){
            this.updateIndex(pageId);
            return (byte[])this.pages.get(pageId);
        }
    }
    public byte[] removePage(final Integer pageId){
        synchronized(this.index){
            this.index.remove(pageId);
            return (byte[])this.pages.remove(pageId);
        }
    }
    public void clear(){
        synchronized(this.index){
            this.index.clear();
            this.pages.clear();
        }
    }
    public int size(){
        return this.pages.size();
    }
    Integer getOldest(){
        return this.index.peek();
    }
    private void updateIndex(final Integer pageId){
        this.index.remove(pageId);
        this.index.offer(pageId);
    }
}
