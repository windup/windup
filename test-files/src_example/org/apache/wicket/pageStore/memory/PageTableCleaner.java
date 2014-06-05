package org.apache.wicket.pageStore.memory;

public class PageTableCleaner{
    public void drop(final PageTable pageTable,final int pagesNumber){
        for(int i=0;i<pagesNumber;++i){
            final Integer pageIdOfTheOldest=pageTable.getOldest();
            pageTable.removePage(pageIdOfTheOldest);
        }
    }
}
