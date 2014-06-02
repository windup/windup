package org.apache.wicket.pageStore.memory;

public class PageNumberEvictionStrategy implements DataStoreEvictionStrategy{
    private final int pagesNumber;
    public PageNumberEvictionStrategy(final int pagesNumber){
        super();
        if(pagesNumber<1){
            throw new IllegalArgumentException("'pagesNumber' must be greater than 0.");
        }
        this.pagesNumber=pagesNumber;
    }
    public void evict(final PageTable pageTable){
        final int size=pageTable.size();
        final int pagesToDrop=size-this.pagesNumber;
        if(pagesToDrop>0){
            final PageTableCleaner cleaner=new PageTableCleaner();
            cleaner.drop(pageTable,pagesToDrop);
        }
    }
}
