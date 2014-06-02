package org.apache.wicket.markup.repeater.data;

import java.util.*;
import org.apache.wicket.markup.repeater.*;
import org.apache.wicket.*;
import org.apache.wicket.model.*;
import org.apache.wicket.util.lang.*;

public abstract class GridView<T> extends DataViewBase<T>{
    private static final long serialVersionUID=1L;
    private int columns;
    private int rows;
    public GridView(final String id,final IDataProvider<T> dataProvider){
        super(id,dataProvider);
        this.columns=1;
        this.rows=Integer.MAX_VALUE;
    }
    public int getColumns(){
        return this.columns;
    }
    public GridView<T> setColumns(final int cols){
        if(cols<1){
            throw new IllegalArgumentException();
        }
        if(this.columns!=cols){
            if(this.isVersioned()){
                this.addStateChange();
            }
            this.columns=cols;
        }
        this.updateItemsPerPage();
        return this;
    }
    public int getRows(){
        return this.rows;
    }
    public GridView<T> setRows(final int rows){
        if(rows<1){
            throw new IllegalArgumentException();
        }
        if(this.rows!=rows){
            if(this.isVersioned()){
                this.addStateChange();
            }
            this.rows=rows;
        }
        this.updateItemsPerPage();
        return this;
    }
    private void updateItemsPerPage(){
        int items=Integer.MAX_VALUE;
        final long result=this.rows*this.columns;
        final int desiredHiBits=-((int)(result>>>31)&0x1);
        final int actualHiBits=(int)(result>>>32);
        if(desiredHiBits==actualHiBits){
            items=(int)result;
        }
        this.setItemsPerPage(items);
    }
    protected void addItems(final Iterator<Item<T>> items){
        if(items.hasNext()){
            final int cols=this.getColumns();
            int row=0;
            do{
                final Item<?> rowItem=this.newRowItem(this.newChildId(),row);
                final RepeatingView rowView=new RepeatingView("cols");
                rowItem.add(rowView);
                this.add(new Component[] { rowItem });
                for(int index=0;index<cols;++index){
                    Item<T> cellItem;
                    if(items.hasNext()){
                        cellItem=(Item<T>)items.next();
                    }
                    else{
                        cellItem=this.newEmptyItem(this.newChildId(),index);
                        this.populateEmptyItem(cellItem);
                    }
                    rowView.add(cellItem);
                }
                ++row;
            } while(items.hasNext());
        }
    }
    public IDataProvider<T> getDataProvider(){
        return this.internalGetDataProvider();
    }
    public Iterator<Item<T>> getItems(){
        final Iterator<MarkupContainer> rows=(Iterator<MarkupContainer>)Generics.iterator((Iterator)this.iterator());
        return new ItemsIterator<T>(rows);
    }
    protected abstract void populateEmptyItem(final Item<T> p0);
    protected Item<T> newEmptyItem(final String id,final int index){
        return new Item<T>(id,index,null);
    }
    protected Item<?> newRowItem(final String id,final int index){
        return new Item<Object>(id,index,null);
    }
    public static class ItemsIterator<T> implements Iterator<Item<T>>{
        private final Iterator<MarkupContainer> rows;
        private Iterator<Item<T>> cells;
        private Item<T> next;
        public ItemsIterator(final Iterator<MarkupContainer> rows){
            super();
            this.rows=(Iterator<MarkupContainer>)Args.notNull((Object)rows,"rows");
            this.findNext();
        }
        public void remove(){
            throw new UnsupportedOperationException();
        }
        public boolean hasNext(){
            return this.next!=null;
        }
        public Item<T> next(){
            final Item<T> item=this.next;
            this.findNext();
            return item;
        }
        private void findNext(){
            this.next=null;
            if(this.cells!=null&&this.cells.hasNext()){
                this.next=(Item<T>)this.cells.next();
            }
            else{
                while(this.rows.hasNext()){
                    final MarkupContainer row=(MarkupContainer)this.rows.next();
                    final Iterator<? extends Component> rawCells=((MarkupContainer)row.iterator().next()).iterator();
                    this.cells=(Iterator<Item<T>>)Generics.iterator((Iterator)rawCells);
                    if(this.cells.hasNext()){
                        this.next=(Item<T>)this.cells.next();
                        break;
                    }
                }
            }
        }
    }
}
