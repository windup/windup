package org.apache.wicket.pageStore;

import java.io.*;
import org.apache.wicket.util.collections.*;
import java.util.*;

public class PageWindowManager implements Serializable{
    private static final long serialVersionUID=1L;
    private final List<PageWindowInternal> windows;
    private IntHashMap<Integer> idToWindowIndex;
    private IntHashMap<Integer> windowIndexToPageId;
    private int indexPointer;
    private int totalSize;
    private final long maxSize;
    private void putWindowIndex(final int pageId,final int windowIndex){
        if(this.idToWindowIndex!=null&&pageId!=-1&&windowIndex!=-1){
            final Integer oldPageId=(Integer)this.windowIndexToPageId.remove(windowIndex);
            if(oldPageId!=null){
                this.idToWindowIndex.remove((int)oldPageId);
            }
            this.idToWindowIndex.put(pageId,(Object)windowIndex);
            this.windowIndexToPageId.put(windowIndex,(Object)pageId);
        }
    }
    private void removeWindowIndex(final int pageId){
        final Integer windowIndex=(Integer)this.idToWindowIndex.remove(pageId);
        if(windowIndex!=null){
            this.windowIndexToPageId.remove((int)windowIndex);
        }
    }
    private void rebuildIndices(){
        this.idToWindowIndex=null;
        this.idToWindowIndex=(IntHashMap<Integer>)new IntHashMap();
        this.windowIndexToPageId=null;
        this.windowIndexToPageId=(IntHashMap<Integer>)new IntHashMap();
        for(int i=0;i<this.windows.size();++i){
            final PageWindowInternal window=(PageWindowInternal)this.windows.get(i);
            this.putWindowIndex(window.pageId,i);
        }
    }
    private int getWindowIndex(final int pageId){
        if(this.idToWindowIndex==null){
            this.rebuildIndices();
        }
        final Integer result=(Integer)this.idToWindowIndex.get(pageId);
        return (result!=null)?result:-1;
    }
    private int incrementIndexPointer(){
        if(this.maxSize>0L&&this.totalSize>=this.maxSize&&this.indexPointer==this.windows.size()-1){
            this.indexPointer=0;
        }
        else{
            ++this.indexPointer;
        }
        return this.indexPointer;
    }
    private int getWindowFileOffset(final int index){
        if(index>0){
            final PageWindowInternal window=(PageWindowInternal)this.windows.get(index-1);
            return window.filePartOffset+window.filePartSize;
        }
        return 0;
    }
    private void splitWindow(final int index,final int size){
        final PageWindowInternal window=(PageWindowInternal)this.windows.get(index);
        final int delta=window.filePartSize-size;
        if(index==this.windows.size()-1){
            this.totalSize-=delta;
            window.filePartSize=size;
        }
        else if(window.filePartSize!=size){
            final PageWindowInternal newWindow=new PageWindowInternal();
            newWindow.pageId=-1;
            window.filePartSize=size;
            this.windows.add(index+1,newWindow);
            newWindow.filePartOffset=this.getWindowFileOffset(index+1);
            newWindow.filePartSize=delta;
        }
        this.idToWindowIndex=null;
        this.windowIndexToPageId=null;
    }
    private void mergeWindowWithNext(final int index){
        if(index<this.windows.size()-1){
            final PageWindowInternal window=(PageWindowInternal)this.windows.get(index);
            final PageWindowInternal next=(PageWindowInternal)this.windows.get(index+1);
            window.filePartSize+=next.filePartSize;
            this.windows.remove(index+1);
            this.idToWindowIndex=null;
            this.windowIndexToPageId=null;
        }
    }
    private void adjustWindowSize(final int index,final int size){
        final PageWindowInternal window=(PageWindowInternal)this.windows.get(index);
        if(index==this.windows.size()-1){
            final int delta=size-window.filePartSize;
            this.totalSize+=delta;
            window.filePartSize=size;
        }
        else{
            while(window.filePartSize<size&&index<this.windows.size()-1){
                this.mergeWindowWithNext(index);
            }
            if(window.filePartSize<size){
                final int delta=size-window.filePartSize;
                this.totalSize+=delta;
                window.filePartSize=size;
            }
            else{
                this.splitWindow(index,size);
            }
        }
        window.pageId=-1;
    }
    private PageWindowInternal allocatePageWindow(final int index,final int size){
        PageWindowInternal window;
        if(index==this.windows.size()){
            window=new PageWindowInternal();
            window.filePartOffset=this.getWindowFileOffset(index);
            this.totalSize+=size;
            window.filePartSize=size;
            this.windows.add(window);
        }
        else{
            window=(PageWindowInternal)this.windows.get(index);
            if(window.filePartSize!=size){
                this.adjustWindowSize(index,size);
            }
        }
        return window;
    }
    public synchronized PageWindow createPageWindow(final int pageId,final int size){
        int index=this.getWindowIndex(pageId);
        if(index!=-1){
            this.removeWindowIndex(pageId);
            ((PageWindowInternal)this.windows.get(index)).pageId=-1;
        }
        if(index==-1||index!=this.indexPointer){
            index=this.incrementIndexPointer();
        }
        final PageWindowInternal window=this.allocatePageWindow(index,size);
        window.pageId=pageId;
        this.putWindowIndex(pageId,index);
        return new PageWindow(window);
    }
    public synchronized PageWindow getPageWindow(final int pageId){
        final int index=this.getWindowIndex(pageId);
        if(index!=-1){
            return new PageWindow((PageWindowInternal)this.windows.get(index));
        }
        return null;
    }
    public synchronized void removePage(final int pageId){
        final int index=this.getWindowIndex(pageId);
        if(index!=-1){
            final PageWindowInternal window=(PageWindowInternal)this.windows.get(index);
            this.removeWindowIndex(pageId);
            if(index==this.windows.size()-1){
                this.windows.remove(index);
                this.totalSize-=window.filePartSize;
                if(this.indexPointer==index){
                    --this.indexPointer;
                }
            }
            else{
                window.pageId=-1;
            }
        }
    }
    public synchronized List<PageWindow> getLastPageWindows(final int count){
        final List<PageWindow> result=(List<PageWindow>)new ArrayList();
        int currentIndex=this.indexPointer;
        while(currentIndex!=-1){
            if(currentIndex<this.windows.size()){
                final PageWindowInternal window=(PageWindowInternal)this.windows.get(currentIndex);
                if(window.pageId!=-1){
                    result.add(new PageWindow(window));
                }
            }
            if(--currentIndex==-1){
                currentIndex=this.windows.size()-1;
            }
            if(result.size()>=count||currentIndex==this.indexPointer){
                return result;
            }
        }
        return result;
    }
    public PageWindowManager(final long maxSize){
        super();
        this.windows=(List<PageWindowInternal>)new ArrayList();
        this.idToWindowIndex=null;
        this.windowIndexToPageId=null;
        this.indexPointer=-1;
        this.totalSize=0;
        this.maxSize=maxSize;
    }
    public synchronized int getTotalSize(){
        return this.totalSize;
    }
    private static class PageWindowInternal implements Serializable{
        private static final long serialVersionUID=1L;
        private int pageId;
        private int filePartOffset;
        private int filePartSize;
    }
    public static class PageWindow{
        private final PageWindowInternal pageWindowInternal;
        private PageWindow(final PageWindowInternal pageWindowInternal){
            super();
            this.pageWindowInternal=pageWindowInternal;
        }
        public int getPageId(){
            return this.pageWindowInternal.pageId;
        }
        public int getFilePartOffset(){
            return this.pageWindowInternal.filePartOffset;
        }
        public int getFilePartSize(){
            return this.pageWindowInternal.filePartSize;
        }
    }
}
